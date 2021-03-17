package cz.vitskalicky.lepsirozvrh.bakaAPI.login

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty


@JsonIgnoreProperties(ignoreUnknown = true)
data class UserResponse(
        val fullName: String? = null,
        val userType: String? = null,
        val userTypeText: String? = null,
        val userUID: String? = null,
        val campaignCategoryCode: String? = null,
        @JsonProperty("Class")
        val clazz: Class? = null,
        val schoolOrganizationName: String? = null,
        val schoolType: Any? = null,
        val studyYear: Int? = null,
        val enabledModules: List<EnabledModule>? = null,
        val settingModules: SettingModules? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Class(
        val id: String? = null,
        val abbrev: String? = null,
        val name: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EnabledModule(
        val module: String? = null,
        val rights: List<String>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SettingModules(
        val common: Common? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Common(
        val type: String? = null,
        val actualSemester: ActualSemester? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ActualSemester(
        val semesterId: String? = null,
        val from: String? = null,
        val to: String? = null
)