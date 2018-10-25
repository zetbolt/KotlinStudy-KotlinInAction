package com.frogsm.kotlininaction

import java.io.File
import java.lang.RuntimeException
import java.text.NumberFormat
import java.util.concurrent.TimeUnit
import kotlin.math.pow

const val IMAGE_TXT_FILE_PATH = "./images.txt"
const val CAPTION_TXT_FILE_PATH = "./captions.txt"

val needToContain = listOf("sky", "cloud", "sunset", "dawn", "sun ", "sun.", "moon", "moon.", "sunrise")
val needToAvoid = listOf("indoor", "bathroom", "kitchen", "selfie", "basement", "bed", "video", "desk", "refrigerator", "food", "pizza", "mirror", "computer", "web", "table", "plate")

fun main(args: Array<String>) {
    val fileExists = File(IMAGE_TXT_FILE_PATH).exists() && File(CAPTION_TXT_FILE_PATH).exists()
    confirmTxtFiles { fileExists }
    makeSure { if (!fileExists) return }

    val images = File(IMAGE_TXT_FILE_PATH)
            .readLines()
            .map { val ss = it.split("\t"); Image(ss[0].toInt(), ss[1]) }
    val captions = File(CAPTION_TXT_FILE_PATH)
            .readLines()
            .map { val ss = it.split("\t"); Caption(ss[1].toInt(), ss[2]) }


    val startNanoTime = System.nanoTime()

    val imageCaptions: List<ImageCaptions> = convertToImageWithCaptions(images, captions)
    val skyImageCaptions: List<ImageCaptions> = filterImageCaptions(imageCaptions, needToContain, needToAvoid)

    val totalTime = System.nanoTime() - startNanoTime

    val nanoTransformer = getTimeTransformer(TimeUnit.NANOSECONDS)      // 1234567 ->  1,234,567 ns
    val milliTransformer = getTimeTransformer(TimeUnit.MILLISECONDS)    // 1234567 ->   1.234567 ms
    val secondTransformer = getTimeTransformer(TimeUnit.SECONDS)        // 1234567 -> 0.001234567 s
    println("spend time : ")
    println("\t${nanoTransformer(totalTime)}")
    println("\t${milliTransformer(totalTime)}")
    println("\t${secondTransformer(totalTime)}")
    println("result : size - ${skyImageCaptions.size} \t ${skyImageCaptions.last()}")

    // Test!
    filteredImageCaptionTest(skyImageCaptions)
}

inline fun confirmTxtFiles(crossinline confirmation: () -> Boolean) {
    val pass = confirmation()
    if (pass) {
        /** You can change codes below here **/
        // TODO - write code
        return
    }
    throw RuntimeException("not touch here")
}

inline fun makeSure(confirmation: () -> Unit) {
    confirmation()
}

fun filterImageCaptions(skyImageCaptions: List<ImageCaptions>,
                        mustContain: List<String>,
                        mustNotContain: List<String>): List<ImageCaptions> {
    // TODO - implementation
    return skyImageCaptions
            .asSequence()
            .filter { imgCaption -> mustContain.any { must -> imgCaption.descriptions.any { it.contains(must) } } }
            .filter { imgCaption -> mustNotContain.none { mustNot -> imgCaption.descriptions.any { it.contains(mustNot) } } }
            .toList()
}

fun convertToImageWithCaptions(images: List<Image>, captions: List<Caption>): List<ImageCaptions> {
    // TODO - implementation
    val captionsGroup = captions
            .asSequence()
            .groupBy { it.imgId } // id 별로 그룹
            .map { it.key to it.value.map { ds -> ds.description } } // Map<ID, Descriptions> 생성
            .toMap()

    return images
            .asSequence()
            .filter { captionsGroup.containsKey(it.imgId) }
            .map { ImageCaptions(it.imgId, it.imgUrl, captionsGroup[it.imgId] ?: emptyList()) }
            .toList()
}

fun getTimeTransformer(unit: TimeUnit): (Long) -> String {
    return when (unit) {
        TimeUnit.NANOSECONDS -> { time ->
            // TODO - 1234567 ->  1,234,567 ns
            "${NumberFormat.getNumberInstance().format(unit.toNanos(time))} ns"
        }
        TimeUnit.MILLISECONDS -> { time ->
            // TODO - 1234567 ->   1.234567 ms
            "${unit.toMillis(time) / 10.0.pow(6)} ms"
        }
        TimeUnit.SECONDS -> { time ->
            // TODO - 1234567 -> 0.001234567 s
            "${unit.toSeconds(time) / 10.0.pow(9)} s"
        }
        else -> {
            throw RuntimeException()
        }
    }
}

/* class */
data class ImageCaptions(
        val imageId: Int,
        val imageUrl: String,
        val descriptions: List<String>
)

data class Image(
        val imgId: Int,
        val imgUrl: String
)

data class Caption(
        val imgId: Int,
        val description: String
)

/* test your code! */
val goodDescription1 = listOf(
        "이 문장들에는 SKY 가 있음",
        "이 문장들에는 needToAvoid 중 어느 것도 포함하지 않음",
        "철수는 SKY 가 있으니 관심이 있음"
)
val goodDescription2 = listOf(
        "이 문장들에는 SKY 가 있음",
        "이 문장들에는 또한 CLOUD 도 있음",
        "철수는 SKY 뿐만 아니라 CLOUD 도 있으니 관심이 있음"
)
val badDescription1 = listOf(
        "이 문장들에는 needToAvoid 뿐만 아니라 needToContain 중 어느 것도 포함하지 않음",
        "철수는 관심이 없음"
)
val badDescription2 = listOf(
        "이 문장들에는 SKY 가 있음",
        "이 문장들에는 INDOOR 도 있음",
        "철수는 SKY 는 있지만 INDOOR 가 있어서 관심이 없음"
)

fun filteredImageCaptionTest(imageCaptions: List<ImageCaptions>) {
    imageCaptions.forEach {
        it.descriptions.forEach { desc ->
            needToAvoid.forEach {
                if (desc.contains(it)) throw RuntimeException("'$desc' has $it")
            }
        }
    }

    imageCaptions.forEach {
        var passed = false
        it.descriptions.forEach { desc ->
            needToContain.forEach {
                if (desc.contains(it)) passed = true
            }
        }
        if (!passed) throw RuntimeException("'${it.descriptions}' hasn't any of needToContain")
    }
}