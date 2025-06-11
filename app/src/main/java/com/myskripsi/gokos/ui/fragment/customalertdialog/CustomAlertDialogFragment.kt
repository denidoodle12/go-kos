package com.myskripsi.gokos.ui.fragment.customalertdialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.myskripsi.gokos.R
import com.myskripsi.gokos.databinding.DialogCustomAlertBinding

class CustomAlertDialogFragment : DialogFragment() {

    private var _binding: DialogCustomAlertBinding? = null
    private val binding get() = _binding!!

    private var onDialogActionListener: (() -> Unit)? = null

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(), // Lebar 90% dari layar
            ViewGroup.LayoutParams.WRAP_CONTENT // Tinggi mengikuti konten
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCustomAlertBinding.inflate(inflater,container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arguments = requireArguments()
        val iconResId = arguments.getInt(ARG_ICON)
        val title = arguments.getString(ARG_TITLE)
        val message = arguments.getString(ARG_MESSAGE)
        val buttonText = arguments.getString(ARG_BUTTON_TEXT)

        binding.ivDialogIcon.setImageResource(iconResId)
        binding.tvDialogTitle.text = title
        binding.tvDialogMessage.text = message
        binding.btnDialogAction.text = buttonText

        binding.btnDialogAction.setOnClickListener {
            onDialogActionListener?.invoke()
            dismiss()
        }

        isCancelable = false
    }

    fun setOnDialogActionListener(listener: () -> Unit) {
        this.onDialogActionListener = listener
    }



    companion object {
        private const val ARG_ICON = "arg_icon"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_MESSAGE = "arg_message"
        private const val ARG_BUTTON_TEXT = "arg_button_text"

        fun newInstance(
            iconResId: Int,
            title: String,
            message: String,
            buttonText: String
        ): CustomAlertDialogFragment {
            val fragment = CustomAlertDialogFragment()
            val args = Bundle().apply {
                putInt(ARG_ICON, iconResId)
                putString(ARG_TITLE, title)
                putString(ARG_MESSAGE, message)
                putString(ARG_BUTTON_TEXT, buttonText)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}