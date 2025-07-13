package com.myskripsi.gokos.ui.fragment.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.myskripsi.gokos.databinding.FragmentEditNoteBinding

class EditNoteBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentEditNoteBinding? = null
    private val binding get() = _binding!!

    private var listener: EditNoteListener? = null

    interface EditNoteListener {
        fun onNoteSaved(newNote: String)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentNote = arguments?.getString(ARG_CURRENT_NOTE)
        binding.etNote.setText(currentNote)

        binding.btnClose.setOnClickListener { dismiss() }

        binding.btnSaveNote.setOnClickListener {
            val newNote = binding.etNote.text.toString().trim()
            listener?.onNoteSaved(newNote)
            dismiss()
        }
    }

    fun setEditNoteListener(listener: EditNoteListener) {
        this.listener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "EditNoteBottomSheet"
        private const val ARG_CURRENT_NOTE = "arg_current_note"

        fun newInstance(currentNote: String?): EditNoteBottomSheet {
            return EditNoteBottomSheet().apply {
                arguments = bundleOf(ARG_CURRENT_NOTE to currentNote)
            }
        }
    }
}