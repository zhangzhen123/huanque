package com.luck.picture.lib.adapter;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.luck.picture.lib.R;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.entity.LocalMediaFolder;
import com.luck.picture.lib.tools.AttrsUtils;
import com.luck.picture.lib.tools.FrescoImageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * author：luck
 * project：PictureSelector
 * package：com.luck.picture.adapter
 * email：893855882@qq.com
 * data：16/12/31
 */
public class PictureAlbumDirectoryAdapter extends RecyclerView.Adapter<PictureAlbumDirectoryAdapter.ViewHolder> {
    private Context mContext;
    private List<LocalMediaFolder> folders = new ArrayList<>();
    private int mimeType;
    private Boolean showImage;

    public PictureAlbumDirectoryAdapter(Context mContext) {
        super();
        this.mContext = mContext;
    }

    public void bindFolderData(List<LocalMediaFolder> folders) {
        this.folders = folders;
        notifyDataSetChanged();
    }

    public void setMimeType(int mimeType) {
        this.mimeType = mimeType;
    }

    public List<LocalMediaFolder> getFolderData() {
        if (folders == null) {
            folders = new ArrayList<>();
        }
        return folders;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.picture_album_folder_item, parent, false);
        showImage = AttrsUtils.getTypeValueBoolean(mContext,
                R.attr.picture_style_album_showImage);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final LocalMediaFolder folder = folders.get(position);
        String name = folder.getName();
        int imageNum = folder.getImageNum();
        String imagePath = folder.getFirstImagePath();
        boolean isChecked = folder.isChecked();
        int checkedNum = folder.getCheckedNum();
        holder.tv_sign.setVisibility(checkedNum > 0 ? View.VISIBLE : View.INVISIBLE);
        holder.itemView.setSelected(isChecked);
        holder.tv_folder_name.setSelected(isChecked);
        holder.image_num.setSelected(isChecked);
        if (mimeType == PictureMimeType.ofAudio()) {
//            holder.first_image.setImageResource(R.drawable.audio_placeholder);
            FrescoImageUtils.loadImageLocal(holder.first_image, R.drawable.audio_placeholder, mContext);
//            ImageUtils.INSTANCE.loadImageLocal(holder.first_image,R.drawable.audio_placeholder);
        } else {
            FrescoImageUtils.loadNativeFilePath(holder.first_image, imagePath, 50f, 50f, mContext);
//            ImageUtils.INSTANCE.loadNativeFilePath(holder.first_image,imagePath,50f,50f);
//            RequestOptions options = new RequestOptions()
//                    .placeholder(R.drawable.ic_placeholder)
//                    .centerCrop()
//                    .sizeMultiplier(0.5f)
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .override(160, 160);
//            Glide.with(holder.itemView.getContext())
//                    .asBitmap()
//                    .load(imagePath)
//                    .apply(options)
//                    .into(new BitmapImageViewTarget(holder.first_image) {
//                        @Override
//                        protected void setResource(Bitmap resource) {
//                            RoundedBitmapDrawable circularBitmapDrawable =
//                                    RoundedBitmapDrawableFactory.
//                                            create(mContext.getResources(), resource);
//                            circularBitmapDrawable.setCornerRadius(8);
//                            holder.first_image.setImageDrawable(circularBitmapDrawable);
//                        }
//                    });
        }
        holder.image_num.setText("(" + imageNum + ")");
        holder.tv_folder_name.setText(name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    for (LocalMediaFolder mediaFolder : folders) {
                        mediaFolder.setChecked(false);
                    }
                    folder.setChecked(true);
                    notifyDataSetChanged();
                    onItemClickListener.onItemClick(folder.getName(), folder.getImages());
                }
            }
        });

        if (showImage) {
            holder.rl_first_image.setVisibility(View.VISIBLE);
        } else {
            holder.rl_first_image.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView first_image;
        TextView tv_folder_name, image_num, tv_sign;
        View rl_first_image;

        public ViewHolder(View itemView) {
            super(itemView);
            first_image = (SimpleDraweeView) itemView.findViewById(R.id.first_image);
            tv_folder_name = (TextView) itemView.findViewById(R.id.tv_folder_name);
            image_num = (TextView) itemView.findViewById(R.id.image_num);
            tv_sign = (TextView) itemView.findViewById(R.id.tv_sign);
            rl_first_image = itemView.findViewById(R.id.rl_first_image);
        }
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(String folderName, List<LocalMedia> images);
    }
}
