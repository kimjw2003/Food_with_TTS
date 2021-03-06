package com.example.food_with_tts.data

data class SchoolBase(
    val schoolInfo: List<SchoolInfo>?
)

data class SchoolInfo(
    val head: List<Head2>?,
    val row: List<SchoolRow>?
)

data class Head2(
    val list_total_count: Number?,
    val result : RESULT2?
)

data class RESULT2(
    val CODE: String?,
    val MESSAGE: String?
)

data class SchoolRow(
    val ATPT_OFCDC_SC_CODE: String?,
    val ATPT_OFCDC_SC_NM: String?,
    val SD_SCHUL_CODE: String?,
    val SCHUL_NM: String?,
    val ENG_SCHUL_NM: String?,
    val SCHUL_KND_SC_NM: String?,
    val LCTN_SC_NM: String?,
    val JU_ORG_NM: String?,
    val FOND_SC_NM: String?,
    val ORG_RDNZC: String?,
    val ORG_RDNMA: String?,
    val ORG_RDNDA: String?,
    val ORG_TELNO: String?,
    val HMPG_ADRES: String?,
    val COEDU_SC_NM: String?,
    val ORG_FAXNO: String?,
    val HS_SC_NM: String?,
    val INDST_SPECL_CCCCL_EXST_YN: String?,
    val HS_GNRL_BUSNS_SC_NM: String?,
    val SPCLY_PURPS_HS_ORD_NM: String?,
    val ENE_BFE_SEHF_SC_NM: String?,
    val DGHT_SC_NM: String?,
    val FOND_YMD: String?,
    val FOAS_MEMRD: String?,
    val LOAD_DTM: String?
)