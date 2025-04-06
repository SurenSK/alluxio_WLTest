/*
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */

package alluxio.worker.block.allocator;

import alluxio.worker.block.BlockMetadataView;
import alluxio.worker.block.BlockStoreLocation;
import alluxio.worker.block.meta.StorageDirView;
import alluxio.worker.block.meta.StorageTierView;
import alluxio.worker.block.reviewer.Reviewer;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.ArrayList;
import java.util.List;

/**
 * A greedy allocator that returns the first Storage dir fitting the size of block to allocate.
 */
@NotThreadSafe
public final class GreedyAllocator implements Allocator {
  private static final Logger LOG = LoggerFactory.getLogger(GreedyAllocator.class);

  private BlockMetadataView mMetadataView;
  private Reviewer mReviewer;

  /**
   * Creates a new instance of {@link GreedyAllocator}.
   *
   * @param view {@link BlockMetadataView} to pass to the allocator
   */
  public GreedyAllocator(BlockMetadataView view) {
    mMetadataView = Preconditions.checkNotNull(view, "view");
    mReviewer = Reviewer.Factory.create();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StorageDirView allocateBlockWithView(long sessionId, long blockSize,
      BlockStoreLocation location, BlockMetadataView metadataView, boolean skipReview) {
    mMetadataView = Preconditions.checkNotNull(metadataView, "view");
    return allocateBlock(sessionId, blockSize, location, skipReview);
  }

  /**
   * Allocates a block from the given block store location.
   *
   * @param sessionId the id of session to apply for the block allocation
   * @param blockSize the size of block in bytes
   * @param location the location in block store
   * @param skipReview whether to skip review when allocating
   * @return a {@link StorageDirView} if allocation succeeds, null otherwise
   */
  @Nullable
  private StorageDirView allocateBlock(long sessionId, long blockSize,
      BlockStoreLocation location, boolean skipReview) {
    Preconditions.checkNotNull(location, "location");
    if (location.equals(BlockStoreLocation.anyTier())) {
      for (StorageTierView tierView : mMetadataView.getTierViews()) {
        List<StorageDirView> sortedDirs = new ArrayList<>(tierView.getDirViews());
        sortedDirs.sort((a, b) -> Integer.compare(a.getDirViewIndex(), b.getDirViewIndex()));
        for (StorageDirView dirView : sortedDirs) {
          if (dirView.getAvailableBytes() >= blockSize) {
            if (skipReview || mReviewer.acceptAllocation(dirView)) {
              return dirView;
            } else {
              LOG.debug("Allocation rejected for anyTier: {}", dirView.toBlockStoreLocation());
            }
          }
        }
      }
      return null;
    }

    String mediumType = location.mediumType();
    if (!mediumType.equals(BlockStoreLocation.ANY_MEDIUM)
        && location.equals(BlockStoreLocation.anyDirInAnyTierWithMedium(mediumType))) {
      for (StorageTierView tierView : mMetadataView.getTierViews()) {
        List<StorageDirView> sortedDirs = new ArrayList<>(tierView.getDirViews());
        sortedDirs.sort((a, b) -> Integer.compare(a.getDirViewIndex(), b.getDirViewIndex()));
        for (StorageDirView dirView : sortedDirs) {
          if (dirView.getMediumType().equals(mediumType)
              && dirView.getAvailableBytes() >= blockSize) {
            if (skipReview || mReviewer.acceptAllocation(dirView)) {
              return dirView;
            } else {
              LOG.debug("Allocation rejected for anyDirInTierWithMedium: {}",
                      dirView.toBlockStoreLocation());
            }
          }
        }
      }
      return null;
    }

    String tierAlias = location.tierAlias();
    StorageTierView tierView = mMetadataView.getTierView(tierAlias);
    if (location.equals(BlockStoreLocation.anyDirInTier(tierAlias))) {
      List<StorageDirView> sortedDirs = new ArrayList<>(tierView.getDirViews());
      sortedDirs.sort((a, b) -> Integer.compare(a.getDirViewIndex(), b.getDirViewIndex()));
      for (StorageDirView dirView : sortedDirs) {
        if (dirView.getAvailableBytes() >= blockSize) {
          if (skipReview || mReviewer.acceptAllocation(dirView)) {
            return dirView;
          } else {
            LOG.debug("Allocation rejected for anyDirInTier: {}",
                    dirView.toBlockStoreLocation());
          }
        }
      }
      return null;
    }

    int dirIndex = location.dir();
    StorageDirView dirView = tierView.getDirView(dirIndex);
    if (dirView != null && dirView.getAvailableBytes() >= blockSize) {
      return dirView;
    }
    return null;
  }
}
