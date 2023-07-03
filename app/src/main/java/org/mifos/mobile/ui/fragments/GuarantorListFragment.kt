package org.mifos.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.Disposable
import org.mifos.mobile.R
import org.mifos.mobile.databinding.FragmentGuarantorListBinding
import org.mifos.mobile.models.guarantor.GuarantorPayload
import org.mifos.mobile.presenters.GuarantorListPresenter
import org.mifos.mobile.ui.activities.base.BaseActivity
import org.mifos.mobile.ui.adapters.GuarantorListAdapter
import org.mifos.mobile.ui.enums.GuarantorState
import org.mifos.mobile.ui.fragments.base.BaseFragment
import org.mifos.mobile.ui.views.GuarantorListView
import org.mifos.mobile.utils.Constants
import org.mifos.mobile.utils.DateHelper
import org.mifos.mobile.utils.RxBus.listen
import org.mifos.mobile.utils.RxEvent.AddGuarantorEvent
import org.mifos.mobile.utils.RxEvent.DeleteGuarantorEvent
import javax.inject.Inject

/*
* Created by saksham on 23/July/2018
*/
@AndroidEntryPoint
class GuarantorListFragment : BaseFragment(), GuarantorListView {

    private var _binding: FragmentGuarantorListBinding? = null
    private val binding get() = _binding!!

    @JvmField
    @Inject
    var presenter: GuarantorListPresenter? = null
    var adapter: GuarantorListAdapter? = null
    var loanId: Long = 0
    var sweetUIErrorHandler: SweetUIErrorHandler? = null
    var list: MutableList<GuarantorPayload?>? = null
    private var disposableAddGuarantor: Disposable? = null
    private var disposableDeleteGuarantor: Disposable? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) loanId = requireArguments().getLong(Constants.LOAN_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentGuarantorListBinding.inflate(inflater, container, false)
        setToolbarTitle(getString(R.string.view_guarantor))
        presenter?.attachView(this)
        if (list == null) {
            presenter?.getGuarantorList(loanId)
            adapter = GuarantorListAdapter(
                context,
                object : GuarantorListAdapter.OnClickListener {
                    override fun setOnClickListener(position: Int) {
                        (activity as BaseActivity?)?.replaceFragment(
                            GuarantorDetailFragment
                                .newInstance(position, loanId, list!![position]),
                            true,
                            R.id.container,
                        )
                    }
                },
            )
            setUpRxBus()
        }
        sweetUIErrorHandler = SweetUIErrorHandler(activity, binding.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabAddLoanGuarantor.setOnClickListener {
            addGuarantor()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (list != null && list?.size == 0) {
            sweetUIErrorHandler?.showSweetCustomErrorUI(
                getString(R.string.no_guarantors),
                getString(R.string.tap_to_add_guarantor),
                R.drawable.ic_person_black_24dp,
                binding.llContainer,
                binding.layoutError.root,
            )
        }
        binding.rvGuarantors.adapter = adapter
        binding.rvGuarantors.layoutManager = LinearLayoutManager(context)
    }

    private fun setUpRxBus() {
        disposableAddGuarantor = listen(AddGuarantorEvent::class.java)
            .subscribe { (payload, index) -> // TODO wrong guarantor id is assigned, although it wont affect the working
                if (index != null) {
                    list?.add(
                        index,
                        GuarantorPayload(
                            list?.size?.toLong(),
                            payload?.officeName,
                            payload?.lastName,
                            payload?.guarantorType,
                            payload?.firstName,
                            DateHelper.getCurrentDate("yyyy-MM-dd", "-"),
                            loanId,
                        ),
                    )
                }
                adapter?.setGuarantorList(list)
            }
        disposableDeleteGuarantor = listen(DeleteGuarantorEvent::class.java)
            .subscribe { (index) ->
                if (index != null) list?.removeAt(index)
                adapter?.setGuarantorList(list)
            }
    }

    fun addGuarantor() {
        (activity as BaseActivity?)?.replaceFragment(
            AddGuarantorFragment.newInstance(
                0,
                GuarantorState.CREATE,
                null,
                loanId,
            ),
            true,
            R.id.container,
        )
    }

    override fun showProgress() {
        showProgressBar()
    }

    override fun hideProgress() {
        hideProgressBar()
    }

    override fun showGuarantorListSuccessfully(list: List<GuarantorPayload?>?) {
        this.list = list as MutableList<GuarantorPayload?>?
        if (list?.size == 0) {
            sweetUIErrorHandler?.showSweetCustomErrorUI(
                getString(R.string.no_guarantors),
                getString(R.string.tap_to_add_guarantor),
                R.drawable.ic_person_black_24dp,
                binding.llContainer,
                binding.layoutError.root,
            )
        } else {
            adapter?.setGuarantorList(list)
        }
    }

    override fun showError(message: String?) {}
    override fun onDestroy() {
        super.onDestroy()
        presenter?.detachView()
        if (disposableAddGuarantor?.isDisposed == false) {
            disposableAddGuarantor?.dispose()
        }
        if (disposableDeleteGuarantor?.isDisposed == false) {
            disposableDeleteGuarantor?.dispose()
        }
        hideProgress()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(loanId: Long?): GuarantorListFragment {
            val fragment = GuarantorListFragment()
            val args = Bundle()
            if (loanId != null) args.putLong(Constants.LOAN_ID, loanId)
            fragment.arguments = args
            return fragment
        }
    }
}
