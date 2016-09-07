package com.example.pierrerivierre.silencr;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import Helpers.Contract;
import Helpers.DBHelp;
import Helpers.Geofencing;

public class ListActivity extends AppCompatActivity {

    ArrayList<ListElem> list;
    ArrayList<ListElem> checkedList;
    public final static String EXTRA_ID = "com.example.tutorial.ID";
    private void checkMode(final ListView listView) {
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id,
                                                  boolean checked) {
                //listView.setItemChecked(position, checked);
                if (checked) {
                    checkedList.add(checkedList.size(), list.get(position));
                } else {
                    checkedList.remove(position);
                }
             }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.context_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case (R.id.delete) :
                        deleteSelectedItems();
                        actionMode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
            }
        });
    }

    private void deleteSelectedItems() {
        DBHelp dbh = new DBHelp(this);
        final Geofencing fence = new Geofencing(this);
        for (int i = 0; i < checkedList.size(); i++) {
            // remove geofences
            Cursor c = dbh.read(Contract.EntrySettings.TABLE_NAME,
                    new String[] {Contract.EntrySettings.COLUMN_NAME_GEO_ID},
                    Contract.EntrySettings.COLUMN_NAME_ENTRY_ID + " = " + checkedList.get(i).id,
                    null, null);

            if (c.moveToFirst()) {
                final long id = c.getLong(
                c.getColumnIndexOrThrow(Contract.EntrySettings.COLUMN_NAME_GEO_ID));
                fence.removeGeofence(id);
            }

            dbh.delete(Contract.EntrySettings.TABLE_NAME,
                    Contract.EntrySettings.COLUMN_NAME_ENTRY_ID + " = " +
                            checkedList.get(i).id, null);
        }
        fence.disconnect();
        dbh.close();
        updateDB();
        checkedList.clear();
    }

    ListElemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        final Context context = this;

    final ListView listview = (ListView) findViewById(R.id.listview);
        checkMode(listview);

        list = new ArrayList<ListElem>();
        checkedList = new ArrayList<ListElem>();
        adapter = new ListElemAdapter(this, list);

    listview.setAdapter(adapter);

    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, final View view,
        int position, long id) {
            final ListElem item = (ListElem) parent.getItemAtPosition(position);
            Intent intent = new Intent(context, SettingsActivity.class);
            intent.putExtra(EXTRA_ID, item.id);
            startActivityForResult(intent, 1);
        }

    });
        updateDB();
}

    public void addToList(View view) {
        ListElem e = new ListElem(new DBHelp(this).insert(Contract.EntrySettings.TABLE_NAME,
                Contract.EntrySettings.BASE_COLUMNS,
                new String[] {"New Item", "Location", "Start Time", "End Time"}));
        list.add(list.size(), e);
        adapter.notifyDataSetChanged();
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(EXTRA_ID, list.get(list.size() - 1).id);
        startActivityForResult(intent, 1);
    }

    private class ListElemAdapter extends ArrayAdapter<ListElem> {

        Context context;
        private LayoutInflater inflater= null;

        public ListElemAdapter(AppCompatActivity activity) {
            super(activity, R.layout.two_lines_list_item);
            context = activity;
            inflater = (LayoutInflater)context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
        }

        public ListElemAdapter(AppCompatActivity activity, List<ListElem> list) {
            super(activity, R.layout.two_lines_list_item, list);
            context = activity;
            inflater = (LayoutInflater)context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return super.getCount();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.two_lines_list_item, parent, false);
            }

            TextView text1 = (TextView) convertView.findViewById(R.id.text1);
            TextView text2 = (TextView) convertView.findViewById(R.id.text2);

            DBHelp dbh = new DBHelp(getApplicationContext());
            Cursor c = dbh.read(Contract.EntrySettings.TABLE_NAME,
                    new String[]{Contract.EntrySettings.COLUMN_NAME_TITLE},
                    Contract.EntrySettings.COLUMN_NAME_ENTRY_ID
                            + " = " + super.getItem(position).id,
                    null,
                    null);

            c.moveToFirst();
            text1.setText(c.getString(c.getColumnIndexOrThrow(
                    Contract.EntrySettings.COLUMN_NAME_TITLE)));

            c.close();
            dbh.close();
            return convertView;
        }
    }

    private class ListElem {
        long id;


        public ListElem(long id) {
            this.id = id;
        }

        public void setId(long newId) {
            id = newId;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                updateDB(data.getLongExtra(EXTRA_ID, -1));
            } else if (resultCode == RESULT_CANCELED) {
                Log.i("Result", "cancelled");
                DBHelp dbh = new DBHelp(this);
                // check if a geofence is defined for this entry

                Cursor c = dbh.read(Contract.EntrySettings.TABLE_NAME,
                        new String[] {Contract.EntrySettings.COLUMN_NAME_GEO_ID},
                        Contract.EntrySettings.COLUMN_NAME_ENTRY_ID + " = "
                                + data.getLongExtra(EXTRA_ID, -1), null, null);

                String message = "";
                Log.i("Count", "" + c.getCount());
                if (c.moveToFirst()) {
                    Log.i("Entered", "moveToFirst");
                    message = c.getString(c.getColumnIndexOrThrow(Contract.EntrySettings.COLUMN_NAME_GEO_ID));
                }

                if (message == null) {
                    Log.i("Entered", "no geo id");
                    // delete this entry
                    dbh.delete(Contract.EntrySettings.TABLE_NAME,
                            Contract.EntrySettings.COLUMN_NAME_ENTRY_ID + " = "
                                    + data.getLongExtra(EXTRA_ID, -1), null);

                    updateDB();
                }

                dbh.close();
            }
        }
    }

    private void updateDB() {
        DBHelp dbh = new DBHelp(this);

        list.clear();

        Cursor c = dbh.read(Contract.EntrySettings.TABLE_NAME, new String[]
                {Contract.EntrySettings.COLUMN_NAME_ENTRY_ID},
                null, null, null);

        while (c.moveToNext()) {
            list.add(list.size(), new ListElem(c.getLong
                    (c.getColumnIndexOrThrow(Contract.EntrySettings.COLUMN_NAME_ENTRY_ID))));
        }

        c.close();
        dbh.close();
        adapter.notifyDataSetChanged();
    }

    private void updateDB(long id) {
        int position = -1;
        for (ListElem e : list) {
            if (e.id == id) {
                position = list.indexOf(e);
            }
        }
        list.remove(position);
        list.add(position, new ListElem(id));
        adapter.notifyDataSetChanged();
    }
}
