package com.kaya.alliancesos.Adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.kaya.alliancesos.Payment.PaymentActivity;
import com.kaya.alliancesos.R;
import com.kaya.alliancesos.Utils.MonthOption;

public class PriceAdapter extends ArrayAdapter<String> {

    private Context mContext;

    public PriceAdapter(@NonNull Context context) {
        super(context, 0);
        mContext = context;
    }

    @Override
    public int getCount() {
        return MonthOption.discount.length;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        convertView = LayoutInflater.from(mContext).inflate(R.layout.price_pattern, parent, false);

        if (position == PaymentActivity.mMonthIdx)
            convertView.setBackground(ContextCompat.getDrawable(mContext, R.drawable.selecte_price_back));
        TextView no_discount = convertView.findViewById(R.id.without_discount);
        TextView discount_txt = convertView.findViewById(R.id.with_discount);
        TextView month_txt = convertView.findViewById(R.id.month_transfer);
        month_txt.setText(MonthOption.months[position] + "  Month ");
        no_discount.setText("$" + MonthOption.prices[position] + " ");
        if (position != 0)
            no_discount.setPaintFlags(no_discount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        discount_txt.setText("$" + MonthOption.discount[position]);
        return convertView;
    }
}
