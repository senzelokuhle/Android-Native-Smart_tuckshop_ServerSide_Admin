package ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.senzosmacbookpro13touchbar.serverside.R;

import Common.Common;
import Interface.ItemClickListener;

public class MenuViewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener,
        View.OnCreateContextMenuListener{

        public TextView txtMenuName;
        public ImageView imageView;

        private ItemClickListener itemClickListener;

        public MenuViewHolder(View itemView) {
            super(itemView);

            txtMenuName=(TextView)itemView.findViewById(R.id.menu_name);
            imageView=(ImageView)itemView.findViewById(R.id.menu_image);

            itemView.setOnCreateContextMenuListener(this);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            itemClickListener.onClick(view,getAdapterPosition(),false);

        }

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle("Select Action");

            contextMenu.add(0,0,getAdapterPosition(), Common.UPDATE);
            contextMenu.add(0,1,getAdapterPosition(), Common.DELETE);

        }

}






