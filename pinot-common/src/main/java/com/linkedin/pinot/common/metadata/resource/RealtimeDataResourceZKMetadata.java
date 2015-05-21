/**
 * Copyright (C) 2014-2015 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.pinot.common.metadata.resource;

import java.util.Map;

import org.apache.helix.ZNRecord;

import com.linkedin.pinot.common.data.Schema;
import com.linkedin.pinot.common.metadata.stream.KafkaStreamMetadata;
import com.linkedin.pinot.common.metadata.stream.StreamMetadata;
import com.linkedin.pinot.common.utils.BrokerRequestUtils;
import com.linkedin.pinot.common.utils.CommonConstants;
import com.linkedin.pinot.common.utils.CommonConstants.Helix.DataSource.Realtime.StreamType;
import com.linkedin.pinot.common.utils.CommonConstants.Helix.TableType;
import static com.linkedin.pinot.common.utils.EqualityUtils.isEqual;
import static com.linkedin.pinot.common.utils.EqualityUtils.hashCodeOf;
import static com.linkedin.pinot.common.utils.EqualityUtils.isSameReference;
import static com.linkedin.pinot.common.utils.EqualityUtils.isNullOrNotSameClass;


@Deprecated
public final class RealtimeDataResourceZKMetadata extends DataResourceZKMetadata {

  private StreamType _streamType;
  private Schema _dataSchema;
  private StreamMetadata _streamMetadata;

  public RealtimeDataResourceZKMetadata() {
    setResourceType(TableType.REALTIME);
  }

  public RealtimeDataResourceZKMetadata(ZNRecord znRecord) {
    super(znRecord);
    setResourceType(TableType.REALTIME);
    _streamType = znRecord.getEnumField(CommonConstants.Helix.DataSource.Realtime.STREAM_TYPE, StreamType.class, StreamType.kafka);
    _dataSchema = Schema.getSchemaFromMap(znRecord.getSimpleFields());
    switch (_streamType) {
      case kafka:
        _streamMetadata = new KafkaStreamMetadata(znRecord.getSimpleFields());
        break;
      default:
        throw new UnsupportedOperationException("Not support stream type - " + _streamType);
    }
  }

  public StreamType getStreamType() {
    return _streamType;
  }

  public void setStreamType(StreamType streamType) {
    _streamType = streamType;
  }

  public Schema getDataSchema() {
    return _dataSchema;
  }

  public void setDataSchema(Schema dataSchema) {
    _dataSchema = dataSchema;
  }

  public StreamMetadata getStreamMetadata() {
    return _streamMetadata;
  }

  public void setStreamMetadata(StreamMetadata streamMetadata) {
    _streamMetadata = streamMetadata;
  }

  /**
   * This is only usable after setting up streamMetadata.
   * 
   * @return streamProvider related config.
   */
  public Map<String, String> getStreamProviderConfig() {
    if (_streamMetadata != null) {
      return _streamMetadata.toMap();
    }
    return null;
  }

  @Override
  public ZNRecord toZNRecord() {
    ZNRecord znRecord = new ZNRecord(BrokerRequestUtils.getRealtimeResourceNameForResource(getResourceName()));
    Map<String, String> fieldMap = _dataSchema.toMap();
    fieldMap.putAll(_streamMetadata.toMap());
    znRecord.setSimpleFields(fieldMap);
    znRecord.merge(super.toZNRecord());
    znRecord.setSimpleField(CommonConstants.Helix.DataSource.Realtime.STREAM_TYPE, _streamType.toString());
    return znRecord;
  }

  @Override
  public boolean equals(Object anotherMetadata) {
    if (isSameReference(this, anotherMetadata)) {
      return true;
    }

    if (isNullOrNotSameClass(this, anotherMetadata)) {
      return false;
    }

    RealtimeDataResourceZKMetadata metadata = (RealtimeDataResourceZKMetadata) anotherMetadata;

    return super.equals(metadata) &&
        isEqual(_streamType, metadata._streamType) &&
        isEqual(_dataSchema, metadata._dataSchema) &&
        isEqual(_streamMetadata, metadata._streamMetadata);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = hashCodeOf(result, _streamType);
    result = hashCodeOf(result, _dataSchema);
    result = hashCodeOf(result, _streamMetadata);
    return result;
  }

  public static RealtimeDataResourceZKMetadata fromZNRecord(ZNRecord record) {
    return new RealtimeDataResourceZKMetadata(record);
  }

  @Override
  public String toString() {
    return "RealtimeDataResourceZKMetadata{" + toZNRecord().toString() + "}";
  }
}
