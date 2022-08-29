
import java.text.SimpleDateFormat
import java.util.*

class TixngoProfile(
    private val firstName: String,
    private val lastName: String,
    private val gender: TixngoGender,
    private val dateOfBirth: Date?,
    private val face: String?,
    private val nationality: String?,
    private val passportNumber: String?,
    private val idCardNumber: String?,
    private val email: String?,
    private val phoneNumber: String?,
    private val address: TixngoAddress?,
    private val birthCity: String?,
    private val birthCountry: String?,
    private val residenceCountry: String?
) {

    fun toJson() : Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        result["firstName"] = firstName
        result["lastName"] = lastName
        result["gender"] = gender.value

        if (face != null) {
            result["face"] = face
        }
        if (dateOfBirth != null) {
            result["dateOfBirth"] = dateOfBirth.toDobString()
        }
        if (nationality != null) {
            result["nationality"] = nationality
        }
        if (passportNumber != null) {
            result["passportNumber"] = passportNumber
        }
        if (idCardNumber != null) {
            result["idCardNumber"] = idCardNumber
        }
        if (email != null) {
            result["email"] = email
        }
        if (phoneNumber != null) {
            result["phoneNumber"] = phoneNumber
        }
        if (address != null) {
            result["address"] = address.toJson()
        }
        if (birthCity != null) {
            result["birthCity"] = birthCity
        }
        if (birthCountry != null) {
            result["birthCountry"] = birthCountry
        }
        if (residenceCountry != null) {
            result["residenceCountry"] = residenceCountry
        }
        return result
    }
}

class TixngoAddress(
    private val line1: String,
    private val line2: String?,
    private val line3: String?,
    private val city: String,
    private val zip: String,
    private val countryCode: String
) {


    fun toJson() : Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        result["line1"] = line1
        result["city"] = city
        result["zip"] = zip
        result["countryCode"] = countryCode
        if (line2 != null) {
            result["line2"] = line2
        }
        if (line3 != null) {
            result["line3"] = line3
        }
        return result
    }
}

class TixngoPushNotification(
    private val data: Map<String, Any>?,
    private val notification: Map<String, Any>?,
) {

    fun toJson() : Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        if (data != null) {
            result["data"] = data
        }
        if (notification != null) {
            result["notification"] = notification
        }
        return result
    }
}

enum class TixngoEnv(val value: String) {
    DEMO("DEMO"),
    INT("INT"),
    VAL ("VAL"),
    PREPROD("PREPROD"),
    PROD("PROD")
}

enum class TixngoGender(val value: String) {
    MALE("male"),
    FEMALE("female"),
    OTHER ("other"),
    UNKNOWN("unknown")
}

fun Date.toDobString(): String {
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale("en_US_POSIX"))
    dateFormatter.calendar = GregorianCalendar()
    return dateFormatter.format(this)
}