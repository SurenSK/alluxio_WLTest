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

package alluxio.util;

import alluxio.client.ReadType;
import alluxio.client.WriteType;
import alluxio.conf.AlluxioConfiguration;
import alluxio.conf.PropertyKey;
import alluxio.grpc.CheckAccessPOptions;
import alluxio.grpc.CheckConsistencyPOptions;
import alluxio.grpc.CreateDirectoryPOptions;
import alluxio.grpc.CreateFilePOptions;
import alluxio.grpc.DeletePOptions;
import alluxio.grpc.ExistsPOptions;
import alluxio.grpc.FileSystemMasterCommonPOptions;
import alluxio.grpc.FreePOptions;
import alluxio.grpc.GetStatusPOptions;
import alluxio.grpc.ListStatusPOptions;
import alluxio.grpc.ListStatusPartialPOptions;
import alluxio.grpc.LoadDescendantPType;
import alluxio.grpc.LoadMetadataPOptions;
import alluxio.grpc.LoadMetadataPType;
import alluxio.grpc.MountPOptions;
import alluxio.grpc.OpenFilePOptions;
import alluxio.grpc.RenamePOptions;
import alluxio.grpc.ScheduleAsyncPersistencePOptions;
import alluxio.grpc.SetAclPOptions;
import alluxio.grpc.SetAttributePOptions;
import alluxio.grpc.SyncMetadataPOptions;
import alluxio.grpc.TtlAction;
import alluxio.grpc.UnmountPOptions;
import alluxio.security.authorization.Mode;
import alluxio.wire.OperationId;

import java.util.UUID;

/**
 * This class contains static methods which can be passed Alluxio configuration objects that
 * will populate the gRPC options objects with the proper values based on the given configuration.
 */
public class FileSystemOptionsUtils {
  /**
   * @param conf Alluxio configuration
   * @return options based on the configuration
   */
  public static ScheduleAsyncPersistencePOptions scheduleAsyncPersistenceDefaults(
      AlluxioConfiguration conf) {
    return ScheduleAsyncPersistencePOptions.newBuilder()
        .setCommonOptions(commonDefaults(conf))
        .setPersistenceWaitTime(0)
        .build();
  }

  /**
   * @param conf Alluxio configuration
   * @return options based on the configuration
   */
  public static CreateDirectoryPOptions createDirectoryDefaults(AlluxioConfiguration conf) {
    return createDirectoryDefaults(conf, true);
  }

  /**
   * @param conf Alluxio configuration
   * @param withOpId Whether to include unique operation-ID in options
   * @return options based on the configuration
   */
  public static CreateDirectoryPOptions createDirectoryDefaults(AlluxioConfiguration conf,
      boolean withOpId) {
    return CreateDirectoryPOptions.newBuilder()
        .setAllowExists(false)
        .setCommonOptions(commonDefaults(conf, withOpId))
        .setMode(ModeUtils.applyDirectoryUMask(Mode.defaults(),
            conf.getString(PropertyKey.SECURITY_AUTHORIZATION_PERMISSION_UMASK)).toProto())
        .setRecursive(false)
        .setWriteType(conf.getEnum(PropertyKey.USER_FILE_WRITE_TYPE_DEFAULT, WriteType.class)
            .toProto())
        .build();
  }

  /**
   * @param conf Alluxio configuration
   * @return options based on the configuration
   */
  public static CheckAccessPOptions checkAccessDefaults(AlluxioConfiguration conf) {
    return CheckAccessPOptions.newBuilder()
        .setCommonOptions(commonDefaults(conf))
        .build();
  }

  /**
   * @param conf Alluxio configuration
   * @return options based on the configuration
   */
  public static CheckConsistencyPOptions checkConsistencyDefaults(AlluxioConfiguration conf) {
    return CheckConsistencyPOptions.newBuilder()
        .setCommonOptions(commonDefaults(conf))
        .build();
  }

  /**
   * @param conf Alluxio configuration
   * @return options based on the configuration
   */
  public static CreateFilePOptions createFileDefaults(AlluxioConfiguration conf) {
    return createFileDefaults(conf, true);
  }

  /**
   * @param conf Alluxio configuration
   * @param withOpId Whether to include unique operation-ID in options
   * @return options based on the configuration
   */
  public static CreateFilePOptions createFileDefaults(AlluxioConfiguration conf, boolean withOpId) {
    return CreateFilePOptions.newBuilder()
        .setBlockSizeBytes(conf.getBytes(PropertyKey.USER_BLOCK_SIZE_BYTES_DEFAULT))
        .setCommonOptions(commonDefaults(conf, withOpId))
        .setMode(ModeUtils.applyFileUMask(Mode.defaults(),
            conf.getString(PropertyKey.SECURITY_AUTHORIZATION_PERMISSION_UMASK)).toProto())
        .setPersistenceWaitTime(conf.getMs(PropertyKey.USER_FILE_PERSISTENCE_INITIAL_WAIT_TIME))
        .setRecursive(false)
        .setReplicationDurable(conf.getInt(PropertyKey.USER_FILE_REPLICATION_DURABLE))
        .setReplicationMax(conf.getInt(PropertyKey.USER_FILE_REPLICATION_MAX))
        .setReplicationMin(conf.getInt(PropertyKey.USER_FILE_REPLICATION_MIN))
        .setWriteTier(conf.getInt(PropertyKey.USER_FILE_WRITE_TIER_DEFAULT))
        .setWriteType(conf.getEnum(PropertyKey.USER_FILE_WRITE_TYPE_DEFAULT, WriteType.class)
            .toProto())
        .build();
  }

  /**
   * @param conf Alluxio configuration
   * @return options based on the configuration
   */
  public static DeletePOptions deleteDefaults(AlluxioConfiguration conf) {
    return deleteDefaults(conf, true);
  }

  /**
   * @param conf Alluxio configuration
   * @param withOpId Whether to include unique operation-ID in options
   * @return options based on the configuration
   */
  public static DeletePOptions deleteDefaults(AlluxioConfiguration conf, boolean withOpId) {
    return DeletePOptions.newBuilder()
        .setAlluxioOnly(false)
        .setCommonOptions(commonDefaults(conf, withOpId))
        .setRecursive(false)
        .setUnchecked(conf.getBoolean(PropertyKey.USER_FILE_DELETE_UNCHECKED))
        .build();
  }

  /**
   * @param conf Alluxio configuration
   * @return options based on the configuration
   */
  public static ExistsPOptions existsDefaults(AlluxioConfiguration conf) {
    return ExistsPOptions.newBuilder()
        .setCommonOptions(commonDefaults(conf))
        .setLoadMetadataType(conf.getEnum(PropertyKey.USER_FILE_METADATA_LOAD_TYPE,
            LoadMetadataPType.class))
        .build();
  }

  /**
   * @param conf Alluxio configuration
   * @return options based on the configuration
   */
  public static SyncMetadataPOptions syncMetadataDefaults(AlluxioConfiguration conf) {
    return SyncMetadataPOptions.newBuilder().build();
  }

  /**
   * @param conf Alluxio configuration
   * @return options based on the configuration
   */
  public static FileSystemMasterCommonPOptions commonDefaults(AlluxioConfiguration conf) {
    return commonDefaults(conf, false);
  }

  /**
   * @param conf Alluxio configuration
   * @param withOpId whether to include a unique operation-id (if also enabled by configuration)
   * @return options based on the configuration
   */
  public static FileSystemMasterCommonPOptions commonDefaults(AlluxioConfiguration conf,
      boolean withOpId) {
    FileSystemMasterCommonPOptions.Builder builder = FileSystemMasterCommonPOptions.newBuilder()
        .setTtl(conf.getMs(PropertyKey.USER_FILE_CREATE_TTL))
        .setTtlAction(conf.getEnum(PropertyKey.USER_FILE_CREATE_TTL_ACTION, TtlAction.class));
    if (withOpId && conf.getBoolean(PropertyKey.USER_FILE_INCLUDE_OPERATION_ID)) {
      builder.setOperationId(new OperationId(UUID.randomUUID()).toFsProto());
    }
    if (conf.isSetByUser(PropertyKey.USER_FILE_METADATA_SYNC_INTERVAL)) {
      builder.setSyncIntervalMs(conf.getMs(PropertyKey.USER_FILE_METADATA_SYNC_INTERVAL));
    }
    return builder.build();
  }

  /**
   * @param conf Alluxio configuration
   * @return options based on the configuration
   */
  public static FreePOptions freeDefaults(AlluxioConfiguration conf) {
    return FreePOptions.newBuilder()
        .setCommonOptions(commonDefaults(conf))
        .setForced(false)
        .setRecursive(false)
        .build();
  }

  /**
   * @param conf Alluxio configuration
   * @return options based on the configuration
   */
  public static GetStatusPOptions getStatusDefaults(AlluxioConfiguration conf) {
    return GetStatusPOptions.newBuilder()
        .setCommonOptions(commonDefaults(conf))
        .setLoadMetadataType(conf.getEnum(PropertyKey.USER_FILE_METADATA_LOAD_TYPE,
            LoadMetadataPType.class))
        .build();
  }

  /**
   * @param conf Alluxio configuration
   * @return options based on the configuration
   */
  public static ListStatusPOptions listStatusDefaults(AlluxioConfiguration conf) {
    return ListStatusPOptions.newBuilder()
        .setCommonOptions(commonDefaults(conf))
        .setLoadMetadataType(conf.getEnum(PropertyKey.USER_FILE_METADATA_LOAD_TYPE,
            LoadMetadataPType.class))
        .setLoadMetadataOnly(false)
        .build();
  }

  /**
   * @param conf Alluxio configuration
   * @return options based on the configuration
   */
  public static ListStatusPartialPOptions listStatusPartialDefaults(AlluxioConfiguration conf) {
    return ListStatusPartialPOptions.newBuilder()
        .setOptions(listStatusDefaults(conf))
        .build();
  }

  /**
   * @param conf Alluxio configuration
   * @return options based on the configuration
   */
  public static LoadMetadataPOptions loadMetadataDefaults(AlluxioConfiguration conf) {
    return LoadMetadataPOptions.newBuilder()
        .setCommonOptions(commonDefaults(conf))
        .setCreateAncestors(false)
        .setLoadDescendantType(LoadDescendantPType.NONE)
        .setRecursive(false)
        .build();
  }

  /**
   * @param conf Alluxio configuration
   * @return options based on the configuration
   */
  public static MountPOptions mountDefaults(AlluxioConfiguration conf) {
    return MountPOptions.newBuilder()
        .setCommonOptions(commonDefaults(conf))
        .setReadOnly(false)
        .setShared(false)
        .build();
  }

  /**
   * @param conf Alluxio configuration
   * @return options based on the configuration
   */
  public static OpenFilePOptions openFileDefaults(AlluxioConfiguration conf) {
    return OpenFilePOptions.newBuilder()
        .setCommonOptions(commonDefaults(conf))
        .setMaxUfsReadConcurrency(conf.getInt(PropertyKey.USER_UFS_BLOCK_READ_CONCURRENCY_MAX))
        .setReadType(conf.getEnum(PropertyKey.USER_FILE_READ_TYPE_DEFAULT, ReadType.class)
            .toProto())
        .build();
  }

  /**
   * @param conf Alluxio configuration
   * @return options based on the configuration
   */
  public static RenamePOptions renameDefaults(AlluxioConfiguration conf) {
    return renameDefaults(conf, true);
  }

  /**
   * @param conf Alluxio configuration
   * @param withOpId Whether to include unique operation-ID in options
   * @return options based on the configuration
   */
  public static RenamePOptions renameDefaults(AlluxioConfiguration conf, boolean withOpId) {
    return RenamePOptions.newBuilder()
        .setCommonOptions(commonDefaults(conf, withOpId))
        .setPersist(conf.getBoolean(PropertyKey.USER_FILE_PERSIST_ON_RENAME))
        .build();
  }

  /**
   * @param conf Alluxio configuration
   * @return options based on the configuration
   */
  public static ScheduleAsyncPersistencePOptions scheduleAsyncPersistDefaults(
      AlluxioConfiguration conf) {
    return ScheduleAsyncPersistencePOptions.newBuilder()
        .setCommonOptions(commonDefaults(conf))
        .build();
  }

  /**
   * @param conf Alluxio configuration
   * @return options based on the configuration
   */
  public static SetAclPOptions setAclDefaults(AlluxioConfiguration conf) {
    return SetAclPOptions.newBuilder()
        .setCommonOptions(commonDefaults(conf))
        .setRecursive(false)
        .build();
  }

  /**
   * @param conf Alluxio configuration
   * @return options based on the configuration
   */
  public static SetAttributePOptions setAttributeDefaults(AlluxioConfiguration conf) {
    return SetAttributePOptions.newBuilder()
        .setCommonOptions(commonDefaults(conf))
        .setRecursive(false)
        .build();
  }

  /**
   * Defaults for the SetAttribute RPC which should only be used on the client side.
   *
   * @param conf Alluxio configuration
   * @return options based on the configuration
   */
  public static SetAttributePOptions setAttributeClientDefaults(AlluxioConfiguration conf) {
    // Specifically set and override *only* the metadata sync interval
    // Setting other attributes by default will make the server think the user is intentionally
    // setting the values. Most fields withinSetAttributePOptions are set by inclusion
    FileSystemMasterCommonPOptions.Builder builder = FileSystemMasterCommonPOptions.newBuilder();
    if (conf.isSetByUser(PropertyKey.USER_FILE_METADATA_SYNC_INTERVAL)) {
      builder.setSyncIntervalMs(conf.getMs(PropertyKey.USER_FILE_METADATA_SYNC_INTERVAL));
    }
    return SetAttributePOptions.newBuilder()
        .setCommonOptions(builder.build())
        .build();
  }

  /**
   * @param conf Alluxio configuration
   * @return options based on the configuration
   */
  public static UnmountPOptions unmountDefaults(AlluxioConfiguration conf) {
    return UnmountPOptions.newBuilder()
        .setCommonOptions(commonDefaults(conf))
        .build();
  }
}
