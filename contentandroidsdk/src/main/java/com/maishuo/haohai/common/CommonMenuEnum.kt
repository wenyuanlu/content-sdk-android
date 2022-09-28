package com.maishuo.haohai.common

import androidx.fragment.app.Fragment
import com.maishuo.haohai.main.ui.HomeMainVoiceCurriculumFragment
import com.maishuo.haohai.main.ui.HomeMainVoiceHeadlinesFragment
import com.maishuo.haohai.main.ui.HomeMainVoiceNovelFragment

/**
 * author : xpSun
 * date : 12/20/21
 * description :
 */
enum class CommonMenuEnum(
    val title: String,
    val fragments: Fragment
) {
    COMMON_MENU_NOVEL("听小说", HomeMainVoiceNovelFragment()),
    COMMON_MENU_HEADLINES("听头条", HomeMainVoiceHeadlinesFragment()),
    COMMON_MENU_CURRICULUM("听课程", HomeMainVoiceCurriculumFragment()),
}