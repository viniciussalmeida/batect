// Code generated by Wire protocol buffer compiler, do not edit.
// Source: moby.filesync.v1.VerifyTokenAuthorityRequest in github.com/moby/buildkit/session/auth/auth.proto
package moby.filesync.v1

import com.squareup.wire.FieldEncoding
import com.squareup.wire.Message
import com.squareup.wire.ProtoAdapter
import com.squareup.wire.ProtoReader
import com.squareup.wire.ProtoWriter
import com.squareup.wire.Syntax.PROTO_3
import com.squareup.wire.WireField
import com.squareup.wire.`internal`.sanitize
import kotlin.Any
import kotlin.AssertionError
import kotlin.Boolean
import kotlin.Deprecated
import kotlin.DeprecationLevel
import kotlin.Int
import kotlin.Long
import kotlin.Nothing
import kotlin.String
import kotlin.Unit
import kotlin.hashCode
import kotlin.jvm.JvmField
import okio.ByteString

public class VerifyTokenAuthorityRequest(
  @field:WireField(
    tag = 1,
    adapter = "com.squareup.wire.ProtoAdapter#STRING",
    label = WireField.Label.OMIT_IDENTITY
  )
  public val Host: String = "",
  @field:WireField(
    tag = 2,
    adapter = "com.squareup.wire.ProtoAdapter#BYTES",
    label = WireField.Label.OMIT_IDENTITY
  )
  public val Payload: ByteString = ByteString.EMPTY,
  @field:WireField(
    tag = 3,
    adapter = "com.squareup.wire.ProtoAdapter#BYTES",
    label = WireField.Label.OMIT_IDENTITY
  )
  public val Salt: ByteString = ByteString.EMPTY,
  unknownFields: ByteString = ByteString.EMPTY
) : Message<VerifyTokenAuthorityRequest, Nothing>(ADAPTER, unknownFields) {
  @Deprecated(
    message = "Shouldn't be used in Kotlin",
    level = DeprecationLevel.HIDDEN
  )
  public override fun newBuilder(): Nothing = throw AssertionError()

  public override fun equals(other: Any?): Boolean {
    if (other === this) return true
    if (other !is VerifyTokenAuthorityRequest) return false
    if (unknownFields != other.unknownFields) return false
    if (Host != other.Host) return false
    if (Payload != other.Payload) return false
    if (Salt != other.Salt) return false
    return true
  }

  public override fun hashCode(): Int {
    var result = super.hashCode
    if (result == 0) {
      result = unknownFields.hashCode()
      result = result * 37 + Host.hashCode()
      result = result * 37 + Payload.hashCode()
      result = result * 37 + Salt.hashCode()
      super.hashCode = result
    }
    return result
  }

  public override fun toString(): String {
    val result = mutableListOf<String>()
    result += """Host=${sanitize(Host)}"""
    result += """Payload=$Payload"""
    result += """Salt=$Salt"""
    return result.joinToString(prefix = "VerifyTokenAuthorityRequest{", separator = ", ", postfix =
        "}")
  }

  public fun copy(
    Host: String = this.Host,
    Payload: ByteString = this.Payload,
    Salt: ByteString = this.Salt,
    unknownFields: ByteString = this.unknownFields
  ): VerifyTokenAuthorityRequest = VerifyTokenAuthorityRequest(Host, Payload, Salt, unknownFields)

  public companion object {
    @JvmField
    public val ADAPTER: ProtoAdapter<VerifyTokenAuthorityRequest> = object :
        ProtoAdapter<VerifyTokenAuthorityRequest>(
      FieldEncoding.LENGTH_DELIMITED, 
      VerifyTokenAuthorityRequest::class, 
      "type.googleapis.com/moby.filesync.v1.VerifyTokenAuthorityRequest", 
      PROTO_3, 
      null
    ) {
      public override fun encodedSize(value: VerifyTokenAuthorityRequest): Int {
        var size = value.unknownFields.size
        if (value.Host != "") size += ProtoAdapter.STRING.encodedSizeWithTag(1, value.Host)
        if (value.Payload != ByteString.EMPTY) size += ProtoAdapter.BYTES.encodedSizeWithTag(2,
            value.Payload)
        if (value.Salt != ByteString.EMPTY) size += ProtoAdapter.BYTES.encodedSizeWithTag(3,
            value.Salt)
        return size
      }

      public override fun encode(writer: ProtoWriter, value: VerifyTokenAuthorityRequest): Unit {
        if (value.Host != "") ProtoAdapter.STRING.encodeWithTag(writer, 1, value.Host)
        if (value.Payload != ByteString.EMPTY) ProtoAdapter.BYTES.encodeWithTag(writer, 2,
            value.Payload)
        if (value.Salt != ByteString.EMPTY) ProtoAdapter.BYTES.encodeWithTag(writer, 3, value.Salt)
        writer.writeBytes(value.unknownFields)
      }

      public override fun decode(reader: ProtoReader): VerifyTokenAuthorityRequest {
        var Host: String = ""
        var Payload: ByteString = ByteString.EMPTY
        var Salt: ByteString = ByteString.EMPTY
        val unknownFields = reader.forEachTag { tag ->
          when (tag) {
            1 -> Host = ProtoAdapter.STRING.decode(reader)
            2 -> Payload = ProtoAdapter.BYTES.decode(reader)
            3 -> Salt = ProtoAdapter.BYTES.decode(reader)
            else -> reader.readUnknownField(tag)
          }
        }
        return VerifyTokenAuthorityRequest(
          Host = Host,
          Payload = Payload,
          Salt = Salt,
          unknownFields = unknownFields
        )
      }

      public override fun redact(value: VerifyTokenAuthorityRequest): VerifyTokenAuthorityRequest =
          value.copy(
        unknownFields = ByteString.EMPTY
      )
    }

    private const val serialVersionUID: Long = 0L
  }
}
