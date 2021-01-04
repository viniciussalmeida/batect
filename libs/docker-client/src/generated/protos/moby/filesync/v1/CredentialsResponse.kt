// Code generated by Wire protocol buffer compiler, do not edit.
// Source: moby.filesync.v1.CredentialsResponse in github.com/moby/buildkit/session/auth/auth.proto
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

public class CredentialsResponse(
  @field:WireField(
    tag = 1,
    adapter = "com.squareup.wire.ProtoAdapter#STRING",
    label = WireField.Label.OMIT_IDENTITY
  )
  public val Username: String = "",
  @field:WireField(
    tag = 2,
    adapter = "com.squareup.wire.ProtoAdapter#STRING",
    label = WireField.Label.OMIT_IDENTITY
  )
  public val Secret: String = "",
  unknownFields: ByteString = ByteString.EMPTY
) : Message<CredentialsResponse, Nothing>(ADAPTER, unknownFields) {
  @Deprecated(
    message = "Shouldn't be used in Kotlin",
    level = DeprecationLevel.HIDDEN
  )
  public override fun newBuilder(): Nothing = throw AssertionError()

  public override fun equals(other: Any?): Boolean {
    if (other === this) return true
    if (other !is CredentialsResponse) return false
    if (unknownFields != other.unknownFields) return false
    if (Username != other.Username) return false
    if (Secret != other.Secret) return false
    return true
  }

  public override fun hashCode(): Int {
    var result = super.hashCode
    if (result == 0) {
      result = unknownFields.hashCode()
      result = result * 37 + Username.hashCode()
      result = result * 37 + Secret.hashCode()
      super.hashCode = result
    }
    return result
  }

  public override fun toString(): String {
    val result = mutableListOf<String>()
    result += """Username=${sanitize(Username)}"""
    result += """Secret=${sanitize(Secret)}"""
    return result.joinToString(prefix = "CredentialsResponse{", separator = ", ", postfix = "}")
  }

  public fun copy(
    Username: String = this.Username,
    Secret: String = this.Secret,
    unknownFields: ByteString = this.unknownFields
  ): CredentialsResponse = CredentialsResponse(Username, Secret, unknownFields)

  public companion object {
    @JvmField
    public val ADAPTER: ProtoAdapter<CredentialsResponse> = object :
        ProtoAdapter<CredentialsResponse>(
      FieldEncoding.LENGTH_DELIMITED, 
      CredentialsResponse::class, 
      "type.googleapis.com/moby.filesync.v1.CredentialsResponse", 
      PROTO_3, 
      null
    ) {
      public override fun encodedSize(value: CredentialsResponse): Int {
        var size = value.unknownFields.size
        if (value.Username != "") size += ProtoAdapter.STRING.encodedSizeWithTag(1, value.Username)
        if (value.Secret != "") size += ProtoAdapter.STRING.encodedSizeWithTag(2, value.Secret)
        return size
      }

      public override fun encode(writer: ProtoWriter, value: CredentialsResponse): Unit {
        if (value.Username != "") ProtoAdapter.STRING.encodeWithTag(writer, 1, value.Username)
        if (value.Secret != "") ProtoAdapter.STRING.encodeWithTag(writer, 2, value.Secret)
        writer.writeBytes(value.unknownFields)
      }

      public override fun decode(reader: ProtoReader): CredentialsResponse {
        var Username: String = ""
        var Secret: String = ""
        val unknownFields = reader.forEachTag { tag ->
          when (tag) {
            1 -> Username = ProtoAdapter.STRING.decode(reader)
            2 -> Secret = ProtoAdapter.STRING.decode(reader)
            else -> reader.readUnknownField(tag)
          }
        }
        return CredentialsResponse(
          Username = Username,
          Secret = Secret,
          unknownFields = unknownFields
        )
      }

      public override fun redact(value: CredentialsResponse): CredentialsResponse = value.copy(
        unknownFields = ByteString.EMPTY
      )
    }

    private const val serialVersionUID: Long = 0L
  }
}