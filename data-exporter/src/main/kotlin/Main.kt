package dev.nikst

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File
import java.net.URI
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream
import kotlin.io.path.absolutePathString

private const val databaseZipUrl =
    "https://www.kaggle.com/api/v1/datasets/download/CooperUnion/anime-recommendations-database"
private const val localDataPath = "data"
private val s3Url = System.getenv("S3_ENDPOINT_URL")
private val s3AccessKey = System.getenv("AWS_ACCESS_KEY_ID")
private val s3SecretKey = System.getenv("AWS_SECRET_ACCESS_KEY")

fun main() {
    downloadAndUnzipData()

    val s3: S3Client = S3Client.builder()
        .endpointOverride(URI.create(s3Url))
        .region(Region.US_EAST_1)
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(s3AccessKey, s3SecretKey)
            )
        )
        .forcePathStyle(true)
        .build()

    while (true) {
        val file = File(localDataPath, "anime.csv")

        upload(s3, file, "anime.csv")

        println("uploaded data to S3")

        TimeUnit.HOURS.sleep(1)
    }
}

fun upload(s3: S3Client, file: File, fileName: String) {
    val request = PutObjectRequest.builder()
        .bucket("data")
        .key(fileName)
        .contentType(URLConnection.guessContentTypeFromName(file.name) ?: "application/octet-stream")
        .build()

    s3.putObject(request, Paths.get(file.toURI()))
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