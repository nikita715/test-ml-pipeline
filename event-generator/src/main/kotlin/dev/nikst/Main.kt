package dev.nikst

import com.opencsv.CSVReader
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.io.File
import java.io.FileReader
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Properties
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream
import kotlin.io.path.absolutePathString

private const val databaseZipUrl =
    "https://www.kaggle.com/api/v1/datasets/download/CooperUnion/anime-recommendations-database"
private const val localDataPath = "data"

private val kafkaBootstrapServer = System.getenv("KAFKA_BOOTSTRAP_SERVERS")
private const val ratingsTopicName = "ratings"
private val schemaRegistryUrl = System.getenv("SCHEMA_REGISTRY_URL")

fun main() {
    downloadAndUnzipData()
    createRatingsTopicIfNotExists()

    val props = Properties().apply {
        put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServer)
        put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer::class.java.name)
        put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl)
    }

    val producer = KafkaProducer<String, GenericRecord>(props)

    val reader = CSVReader(FileReader("$localDataPath/rating.csv"))
    val lines = reader.readAll()
    val header = lines.first()
    val rows = lines.drop(1).shuffled()

    val schema = Schema.Parser().parse(ratingSchema)

    for (row in rows) {
        val map = header.zip(row).toMap()

        val rating: GenericRecord = GenericData.Record(schema).apply {
            put("userId", map.getValue("user_id").toLong())
            put("animeId", map.getValue("anime_id").toLong())
            put("rating", map.getValue("rating").toInt())
        }

        println(rating)

        val record = ProducerRecord(ratingsTopicName, rating["userId"].toString(), rating)
        producer.send(record) { metadata, exception ->
            if (exception != null) {
                println("❌ Error sending: ${exception.message}")
            } else {
                println("✅ Sent to ${metadata.topic()} [${metadata.partition()}] offset=${metadata.offset()}")
            }
        }

        TimeUnit.MILLISECONDS.sleep(100)
    }

    producer.flush()
    producer.close()
}

fun downloadAndUnzipData() {
    if (File(localDataPath).exists()) {
        println("file already exists")
        return
    }

    val tempZip = Files.createTempFile("download", ".zip")
    URI(databaseZipUrl).toURL().openStream().use { input ->
        Files.copy(input, tempZip, StandardCopyOption.REPLACE_EXISTING)
    }
    println("downloaded ${tempZip.absolutePathString()}")

    ZipInputStream(Files.newInputStream(tempZip)).use { zip ->
        var entry = zip.nextEntry
        while (entry != null) {
            val outPath = File(localDataPath).resolve(entry.name).toPath()
            if (entry.isDirectory) {
                Files.createDirectories(outPath)
            } else {
                Files.createDirectories(outPath.parent)
                Files.newOutputStream(outPath).use { output ->
                    zip.copyTo(output)
                }
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }
    }
}

fun createRatingsTopicIfNotExists() {
    val props = Properties().apply {
        put("bootstrap.servers", listOf(kafkaBootstrapServer))
    }

    AdminClient.create(props).use { admin ->
        val existingTopics = admin.listTopics().names().get(10, TimeUnit.SECONDS)
        if (ratingsTopicName in existingTopics) {
            println("Топик '$ratingsTopicName' уже существует")
        } else {
            val newTopic = NewTopic(ratingsTopicName, 4, 1.toShort())
            admin.createTopics(listOf(newTopic)).all().get(10, TimeUnit.SECONDS)
            println("Топик '$ratingsTopicName' создан")
        }
    }
}

private val ratingSchema = """
{
  "type": "record",
  "name": "Rating",
  "namespace": "com.example.avro",
  "fields": [
    {"name": "userId", "type": "long"},
    {"name": "animeId", "type": "long"},
    {"name": "rating", "type": "int"}
  ]
}
""".trimIndent()