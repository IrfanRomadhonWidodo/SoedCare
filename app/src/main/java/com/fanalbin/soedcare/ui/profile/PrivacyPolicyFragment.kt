package com.fanalbin.soedcare.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.fanalbin.soedcare.R

class PrivacyPolicyFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_privacy_policy, container, false)

        // Setup expandable sections
        setupExpandableSection(
            view.findViewById(R.id.header_info_collection),
            view.findViewById(R.id.content_info_collection),
            view.findViewById(R.id.arrow_info_collection)
        )

        setupExpandableSection(
            view.findViewById(R.id.header_info_usage),
            view.findViewById(R.id.content_info_usage),
            view.findViewById(R.id.arrow_info_usage)
        )

        setupExpandableSection(
            view.findViewById(R.id.header_info_protection),
            view.findViewById(R.id.content_info_protection),
            view.findViewById(R.id.arrow_info_protection)
        )

        setupExpandableSection(
            view.findViewById(R.id.header_info_sharing),
            view.findViewById(R.id.content_info_sharing),
            view.findViewById(R.id.arrow_info_sharing)
        )

        setupExpandableSection(
            view.findViewById(R.id.header_privacy_rights),
            view.findViewById(R.id.content_privacy_rights),
            view.findViewById(R.id.arrow_privacy_rights)
        )

        setupExpandableSection(
            view.findViewById(R.id.header_policy_changes),
            view.findViewById(R.id.content_policy_changes),
            view.findViewById(R.id.arrow_policy_changes)
        )

        setupExpandableSection(
            view.findViewById(R.id.header_contact_us),
            view.findViewById(R.id.content_contact_us),
            view.findViewById(R.id.arrow_contact_us)
        )

        return view
    }

    private fun setupExpandableSection(
        header: LinearLayout,
        content: LinearLayout,
        arrow: ImageView
    ) {
        header.setOnClickListener {
            if (content.visibility == View.VISIBLE) {
                content.visibility = View.GONE
                arrow.setImageResource(R.drawable.ic_expand_more)
            } else {
                content.visibility = View.VISIBLE
                arrow.setImageResource(R.drawable.ic_expand_less)
            }
        }
    }
}