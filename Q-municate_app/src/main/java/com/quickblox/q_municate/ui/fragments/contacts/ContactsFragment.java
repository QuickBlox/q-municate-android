package com.quickblox.q_municate.ui.fragments.contacts;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.adapters.contacts.ContactsViewPagerAdapter;
import com.quickblox.q_municate.ui.fragments.base.BaseFragment;
import com.quickblox.q_municate.ui.fragments.chats.DialogsListFragment;
import com.quickblox.q_municate.utils.KeyboardUtils;

import butterknife.Bind;

public class ContactsFragment extends BaseFragment implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    @Bind(R.id.contacts_viewpager)
    ViewPager contactsViewPager;

    @Bind(R.id.contacts_radiogroup)
    RadioGroup modeRadioGroup;

    private ContactsViewPagerAdapter contactsPagerAdapter;

    public static ContactsFragment newInstance() {
        return new ContactsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.fragment_contacts, container, false);

        activateButterKnife(view);

        initViewPagerAdapter();
        initCustomListeners();

        return view;
    }

    @Override
    public void initActionBar() {
        super.initActionBar();
        actionBarBridge.setActionBarUpButtonEnabled(true);
        actionBarBridge.setActionBarTitle(R.string.action_bar_contacts);
    }

    private void initViewPagerAdapter() {
        contactsPagerAdapter = new ContactsViewPagerAdapter(getChildFragmentManager());
        contactsViewPager.setAdapter(contactsPagerAdapter);
        contactsViewPager.setOnPageChangeListener(new PageChangeListener());
        modeRadioGroup.check(R.id.your_contacts_radiobutton);
    }

    private void initCustomListeners() {
        modeRadioGroup.setOnCheckedChangeListener(new RadioGroupListener());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.contacts_list_menu, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = null;

        if (searchMenuItem != null) {
            searchView = (SearchView) searchMenuItem.getActionView();
        }

        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
            searchView.setOnQueryTextListener(this);
            searchView.setOnCloseListener(this);
            searchView.onActionViewExpanded();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                launchDialogsListFragment();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String searchQuery) {
        KeyboardUtils.hideKeyboard(baseActivity);

        if (contactsPagerAdapter != null && contactsViewPager != null) {
            contactsPagerAdapter.search(contactsViewPager.getCurrentItem(), searchQuery);
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String searchQuery) {
        if (contactsPagerAdapter != null && contactsViewPager != null) {
            contactsPagerAdapter.search(contactsViewPager.getCurrentItem(), searchQuery);
        }
        return true;
    }

    @Override
    public boolean onClose() {
        if (contactsPagerAdapter != null && contactsViewPager != null) {
            contactsPagerAdapter.cancelSearch(contactsViewPager.getCurrentItem());
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        KeyboardUtils.hideKeyboard(getActivity());
    }

    private void launchDialogsListFragment() {
        baseActivity.setCurrentFragment(DialogsListFragment.newInstance());
    }

    private class RadioGroupListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
            switch (checkedId) {
                case R.id.your_contacts_radiobutton:
                    contactsViewPager.setCurrentItem(ContactsViewPagerAdapter.POSITION_YOUR_CONTACTS);
                    contactsPagerAdapter.prepareSearch(ContactsViewPagerAdapter.POSITION_YOUR_CONTACTS);
                    break;
                case R.id.all_users_radiobutton:
                    contactsViewPager.setCurrentItem(ContactsViewPagerAdapter.POSITION_ALL_USERS);
                    contactsPagerAdapter.prepareSearch(ContactsViewPagerAdapter.POSITION_ALL_USERS);
                    break;
            }
        }
    }

    private class PageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case ContactsViewPagerAdapter.POSITION_YOUR_CONTACTS:
                    modeRadioGroup.check(R.id.your_contacts_radiobutton);
                    break;
                case ContactsViewPagerAdapter.POSITION_ALL_USERS:
                    modeRadioGroup.check(R.id.all_users_radiobutton);
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }
}