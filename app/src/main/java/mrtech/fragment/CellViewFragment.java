package mrtech.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import mrtech.tv.R;

/**
 * Created by sphynx on 2016/1/14.
 */
public abstract class CellViewFragment extends Fragment {

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toast.makeText(getActivity(), ""+getFragmentManager().getFragments().size(), Toast.LENGTH_SHORT).show();
    }

    private List<View> findAllCell(TableLayout tableLayout) {
        final List<TableRow> rows = findAllView(tableLayout, TableRow.class);
        ArrayList<View> result = new ArrayList<>();
        for (TableRow row : rows) {
            result.addAll(findAllView(row, View.class));
        }
        return result;
    }

    private <T extends View> List<T> findAllView(ViewGroup group, Class<T> cls) {
        final int childCount = group.getChildCount();
        ArrayList<T> result = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            final View child = group.getChildAt(i);
            if (child.getClass().isAssignableFrom(cls)) {
                result.add((T) child);
            }
        }
        return result;
    }


}
