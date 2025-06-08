package com.myskripsi.gokos.ui.fragment.customalertdialog // Atau di package yang sama dengan dialog sebelumnya

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.myskripsi.gokos.databinding.FragmentConfirmationDialogBinding

class ConfirmationDialogFragment : DialogFragment() {

    private var _binding: FragmentConfirmationDialogBinding? = null
    private val binding get() = _binding!!

    private var listener: ConfirmationDialogListener? = null

    interface ConfirmationDialogListener {
        fun onConfirm()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(), // Lebar 90% dari layar
            ViewGroup.LayoutParams.WRAP_CONTENT // Tinggi mengikuti konten
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? ConfirmationDialogListener ?: context as? ConfirmationDialogListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentConfirmationDialogBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ambil data dari arguments
        val arguments = requireArguments()
        binding.tvDialogTitle.text = arguments.getString(ARG_TITLE)
        binding.tvDialogMessage.text = arguments.getString(ARG_MESSAGE)
        binding.btnPositive.text = arguments.getString(ARG_POSITIVE_BUTTON_TEXT)
        binding.btnNegative.text = arguments.getString(ARG_NEGATIVE_BUTTON_TEXT)

        binding.btnPositive.setOnClickListener {
            listener?.onConfirm()
            dismiss()
        }

        binding.btnNegative.setOnClickListener {
            dismiss()
        }

        isCancelable = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        // Tambahkan argumen baru
        private const val ARG_TITLE = "arg_title"
        private const val ARG_MESSAGE = "arg_message"
        private const val ARG_POSITIVE_BUTTON_TEXT = "arg_positive_button_text"
        private const val ARG_NEGATIVE_BUTTON_TEXT = "arg_negative_button_text"

        // Perbarui factory method untuk menerima teks custom
        fun newInstance(
            title: String,
            message: String,
            positiveButtonText: String,
            negativeButtonText: String
        ): ConfirmationDialogFragment {
            val fragment = ConfirmationDialogFragment()
            val args = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_MESSAGE, message)
                putString(ARG_POSITIVE_BUTTON_TEXT, positiveButtonText)
                putString(ARG_NEGATIVE_BUTTON_TEXT, negativeButtonText)
            }
            fragment.arguments = args
            return fragment
        }
    }
}