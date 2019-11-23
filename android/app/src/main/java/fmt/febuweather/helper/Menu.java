package fmt.febuweather.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.navdrawer.SimpleSideDrawer;
import fmt.febuweather.R;


@SuppressLint("Registered")
public class Menu extends Activity{


    ListView MenuList;
    ArrayList<MenuListItem> MenuItemList;
    MenuListAdapter MenuListAdapter;

    String[] MenuTitles;
    TypedArray MenuIcons;

    public SimpleSideDrawer LeftDrawer;

    Activity menuActivity;


    @SuppressWarnings("ResourceType")
    public Menu(Activity activity){

        menuActivity = activity;

        LeftDrawer = new SimpleSideDrawer(menuActivity);
        LeftDrawer.setLeftBehindContentView(R.layout.activity_menu);

        MenuItemList = new ArrayList<>();
        MenuTitles = menuActivity.getResources().getStringArray(R.array.menu_titles);
        MenuIcons = menuActivity.getResources().obtainTypedArray(R.array.menu_icons);

        MenuItemList.add(new MenuListItem(MenuTitles[0], MenuIcons.getResourceId(0, -1)));
        MenuItemList.add(new MenuListItem(MenuTitles[1], MenuIcons.getResourceId(1, -1)));
        MenuItemList.add(new MenuListItem(MenuTitles[2], MenuIcons.getResourceId(2, -1)));
        MenuItemList.add(new MenuListItem(MenuTitles[3], MenuIcons.getResourceId(3, -1)));
        MenuItemList.add(new MenuListItem(MenuTitles[4], MenuIcons.getResourceId(4, -1)));
        MenuItemList.add(new MenuListItem(MenuTitles[5], MenuIcons.getResourceId(5, -1)));
        MenuItemList.add(new MenuListItem(MenuTitles[6], MenuIcons.getResourceId(6, -1)));
        MenuItemList.add(new MenuListItem(MenuTitles[7], MenuIcons.getResourceId(7, -1)));

        MenuIcons.recycle();

        MenuList = menuActivity.findViewById(R.id.me_list);
        MenuListAdapter = new MenuListAdapter(menuActivity, MenuItemList);
        MenuList.setAdapter(MenuListAdapter);
        MenuList.setOnItemClickListener(new SlideMenuClickListener());
        
    }


    private class MenuListAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<MenuListItem> menuListItems;

        private MenuListAdapter(Context context, ArrayList<MenuListItem> menuListItems){

            this.context = context;
            this.menuListItems = menuListItems;

        }

        @Override
        public int getCount() {
            return menuListItems.size();
        }

        @Override
        public Object getItem(int position) {
            return menuListItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {

                LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                assert mInflater != null;
                convertView = mInflater.inflate(R.layout.activity_me_list_item, parent, false);

            }

            ImageView imgIcon = convertView.findViewById(R.id.me_li_icon);
            TextView txtTitle = convertView.findViewById(R.id.me_li_title);

            imgIcon.setImageResource(menuListItems.get(position).getIcon());
            txtTitle.setText(menuListItems.get(position).getTitle());

            return convertView;

        }
    }


    private class MenuListItem {

        private String title;
        private int icon;

        private MenuListItem(String title, int icon){

            this.title = title;
            this.icon = icon;

        }

        private String getTitle(){
            return this.title;
        }

        private int getIcon(){
            return this.icon;
        }

    }


    private class SlideMenuClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            displayView(position);

        }

    }


    private void displayView(int position) {

        switch (position) {

            case 0:

                Intent MyLocation = new Intent(menuActivity, fmt.febuweather.MyLocation.class);
                menuActivity.startActivity(MyLocation);
                menuActivity.finish();
                break;

            case 1:

                Intent OtherLocations = new Intent(menuActivity, fmt.febuweather.OtherLocations.class);
                menuActivity.startActivity(OtherLocations);
                menuActivity.finish();
                break;

            case 2:

                Intent Settings = new Intent(menuActivity, fmt.febuweather.Settings.class);
                menuActivity.startActivity(Settings);
                menuActivity.finish();
                break;

            case 3:

                Intent Gallery = new Intent(menuActivity, fmt.febuweather.Gallery.class);
                menuActivity.startActivity(Gallery);
                menuActivity.finish();
                break;

            case 4:

                Intent ContactUs = new Intent(menuActivity, fmt.febuweather.ContactUs.class);
                menuActivity.startActivity(ContactUs);
                menuActivity.finish();
                break;

            case 5:

                String appPackageName = menuActivity.getPackageName();

                try {

                    menuActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));

                } catch (android.content.ActivityNotFoundException anfe) {

                    menuActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));

                }

                break;

            case 6:

                String appDeveloperId = "8919005167984199925";

                try {

                    menuActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://dev?id=" + appDeveloperId)));

                } catch (android.content.ActivityNotFoundException anfe) {

                    menuActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/dev?id=" + appDeveloperId)));

                }

                break;

            case 7:

                menuActivity.finish();

                break;

            default:
                break;

        }
    }


    public void onBackPressed() {

        if (getFragmentManager().getBackStackEntryCount() > 0) {

            getFragmentManager().popBackStack();

        } else {

            super.onBackPressed();

        }

    }

}