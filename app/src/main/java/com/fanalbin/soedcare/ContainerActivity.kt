package com.fanalbin.soedcare

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.fanalbin.soedcare.ui.ask_soedcare.CreateQuestionFragment
import com.fanalbin.soedcare.ui.ask_soedcare.QuestionDetailFragment
import com.fanalbin.soedcare.ui.base.BaseActivity

class ContainerActivity : BaseActivity() {

    companion object {
        const val FRAGMENT_TYPE = "fragment_type"
        const val FRAGMENT_CREATE_QUESTION = "create_question"
        const val FRAGMENT_QUESTION_DETAIL = "question_detail"
        const val QUESTION_ID = "question_id"
    }

    private var fragmentType: String? = null
    private var questionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        fragmentType = intent.getStringExtra(FRAGMENT_TYPE)
        questionId = intent.getStringExtra(QUESTION_ID)
        super.onCreate(savedInstanceState)
    }

    override fun createFragment(): Fragment {
        return when (fragmentType) {
            FRAGMENT_CREATE_QUESTION -> CreateQuestionFragment()
            FRAGMENT_QUESTION_DETAIL -> {
                val fragment = QuestionDetailFragment()
                val args = Bundle()
                args.putString("QUESTION_ID", questionId)
                fragment.arguments = args
                fragment
            }
            else -> throw IllegalArgumentException("Unknown fragment type")
        }
    }

    override fun getToolbarTitle(): String {
        return when (fragmentType) {
            FRAGMENT_CREATE_QUESTION -> "Buat Pertanyaan Baru"
            FRAGMENT_QUESTION_DETAIL -> "Detail Pertanyaan"
            else -> ""
        }
    }
}