//import com.bridgecrew.analytics.AnalyticsData
//import kotlinx.serialization.KSerializer
//import kotlinx.serialization.SerialName
//import kotlinx.serialization.Serializable
//import kotlinx.serialization.builtins.ListSerializer
//import kotlinx.serialization.decodeFromString
//import kotlinx.serialization.json.Json
//import kotlinx.serialization.json.Json.Default.decodeFromString
//import kotlinx.serialization.modules.SerializersModule
//import kotlinx.serialization.modules.polymorphic
//import kotlinx.serialization.modules.subclass
//import kotlinx.serialization.*
//import kotlinx.serialization.descriptors.PolymorphicKind
//import kotlinx.serialization.descriptors.SerialDescriptor
//import kotlinx.serialization.descriptors.buildSerialDescriptor
//import kotlinx.serialization.encoding.Decoder
//import kotlinx.serialization.encoding.Encoder
//import kotlinx.serialization.json.*
//
//
//
//
//@Serializable
//sealed class BaseEventData
//
//@Serializable
//data class DataClassA(val eventData: String) : BaseEventData()
//
//@Serializable
//data class DataClassB(val eventData: Int) : BaseEventData()
//
//
//
//
//fun main() {
//
//
//    val dataA = DataClassA("ValueA")
//    val dataB = DataClassB(42)
//
//    val json = Json { }
//
//    val jsonStringA = json.encodeToString(DataClassA.serializer(), dataA)
//    val jsonStringB = json.encodeToString(DataClassB.serializer(), dataB)
//
//     var analyticsEventData: MutableList<String> = arrayListOf()
//    analyticsEventData.add(jsonStringA)
//    analyticsEventData.add(jsonStringB)
//
//    val jsonArray = analyticsEventData.joinToString(prefix = "[", postfix = "]")
//
////    val jsonArray = "[$jsonStringA, $jsonStringB]"
//
//    println(jsonArray)
//}
