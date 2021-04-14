package ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.senzosmacbookpro13touchbar.serverside.R;

import Interface.ItemClickListener;


    public class OrderViewHolder extends RecyclerView.ViewHolder
    {

        public TextView txtOrderId,txtOrderStatus,txtOrderPhone,txtOrderAddress, txtOrderSize;

        public Button btnEdit,btnRemove, btnDetail,btnDirection;


        public OrderViewHolder(View itemView) {
            super(itemView);

            txtOrderAddress=(TextView)itemView.findViewById(R.id.order_address);
            txtOrderId=(TextView)itemView.findViewById(R.id.order_id);
            txtOrderPhone=(TextView)itemView.findViewById(R.id.order_phone);
            txtOrderStatus=(TextView)itemView.findViewById(R.id.order_status);


            btnEdit=(Button)itemView.findViewById(R.id.btnEdit);
            btnDetail=(Button)itemView.findViewById(R.id.btnDetail);
            btnRemove=(Button)itemView.findViewById(R.id.btnRemove);
            btnDirection=(Button)itemView.findViewById(R.id.btnDirection);

        }

    }


