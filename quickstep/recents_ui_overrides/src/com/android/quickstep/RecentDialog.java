package com.android.quickstep;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.app.ActivityManager;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.TextView;

import com.android.launcher3.R;
import com.android.launcher3.testing.TestLogging;
import com.android.launcher3.testing.TestProtocol;
import com.android.quickstep.views.IconView;
import com.android.quickstep.views.TaskThumbnailView;
import com.android.systemui.shared.recents.model.Task;
import java.util.ArrayList;
import java.util.function.Consumer;
import com.android.systemui.shared.recents.model.Task;
import com.android.systemui.shared.recents.model.Task.TaskKey;
import com.android.systemui.shared.recents.model.ThumbnailData;
import com.android.systemui.shared.system.ActivityManagerWrapper;


public class RecentDialog extends Dialog {
    Context mContext;
    View mView;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ImageView mImageEmpty;
    private TextView mTvEmpty;
    private ArrayList<Task> mRecentApps = new ArrayList<>();
    private Task mRecentApp;

    private void initDialog(Context context) {
        mContext  = context;
        mView = View.inflate(mContext, R.layout.recent, null);
        setContentView(mView);
        mView.setMinimumHeight((int) (ScreenSizeUtils.getInstance(mContext).getScreenHeight() * 0.23f));
        setCanceledOnTouchOutside(true);
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) (ScreenSizeUtils.getInstance(mContext).getScreenWidth() * 0.72f);
        dialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mAdapter.notifyDataSetChanged();
                showEmpty(mRecentApps.size() == 0);
            }
        });
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(mRecentApp != null){
                    gotoTask(mRecentApp);
                }
            }
        });
    }


    private void initData() {
        mRecyclerView = mView.findViewById(R.id.recycle);
        mImageEmpty = mView.findViewById(R.id.empty);
        mTvEmpty = mView.findViewById(R.id.tvEmpty);
        showEmpty(mRecentApps.size() == 0);

        mAdapter = new RecyclerView.Adapter<ViewHolder>() {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
                return new ViewHolder(getLayoutInflater().inflate(R.layout.recent_item, viewGroup, false), mContext);
            }

            @Override
            public void onBindViewHolder(ViewHolder viewHolder, int position) {
                Task task = mRecentApps.get(position);
                viewHolder.binderRecentApp(task);
                viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RecentDialog.this.dismiss();
                        gotoTask(task);
                    }
                });

                viewHolder.imageClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(task != null){
                            ActivityManagerWrapper.getInstance().removeTask(task.key.id);
                            mRecentApps.remove(task);
                            if(mRecentApps.size() == 0 ){
                                mRecentApp = null;
                                dismiss();
                            } else {
                                mAdapter.notifyDataSetChanged();
                                showEmpty(mRecentApps.size() == 0);
                            }
                        }
                    }
                });
            }

            @Override
            public int getItemCount() {
                return mRecentApps.size();
            }
        };
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));
        mRecyclerView.setAdapter(mAdapter);
    }

    private void showEmpty(boolean empty) {
        if(empty){
            mImageEmpty.setVisibility(View.VISIBLE);
            mTvEmpty.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            mRecentApp = null;
        } else {
            mImageEmpty.setVisibility(View.GONE);
            mTvEmpty.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            if(mRecentApps.size() > 1){
                mRecentApp = mRecentApps.get(mRecentApps.size()-2);
            }else{
                mRecentApp = mRecentApps.get(mRecentApps.size()-1);
            } 
        }
    }

    private void gotoTask(Task mTask) {
        if (mTask != null) {
            ActivityManagerWrapper.getInstance().startActivityFromRecents(mTask.key.id,
                    null);
            mRecentApp = null;
        }
    }



    public RecentDialog(Context context, int normalDialogStyle, ArrayList<Task> tasks) {
        super(context);
        this.mRecentApps.clear();
        this.mRecentApps.addAll(tasks);
        initDialog(context);
        initData();
    }


    public RecentDialog(Context context, int themeResId) {
        super(context, themeResId);
        initDialog(context);
        initData();
    }

    protected RecentDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        initDialog(context);
        initData();
    }

    public void setRecentTasks(ArrayList<Task> tasks) {
        this.mRecentApps.clear();
        this.mRecentApps.addAll(tasks);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TaskThumbnailCache.ThumbnailLoadRequest mThumbnailLoadRequest;
        private TaskIconCache.IconLoadRequest mIconLoadRequest;


        public IconView mIconView;
        private Context mContext;
        public ImageView imageView;
        public TextView tvName;
        public ImageView imageClose;

        public ViewHolder(@NonNull View itemView, Context context ) {
            super(itemView);
            this.mContext = context;
        }

        public void binderRecentApp(Task  mTask) {
            ActivityManager am= (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            am.moveTaskToBack(true, mTask.key.id);
            mIconView = itemView.findViewById(R.id.icon);
            imageView = itemView.findViewById(R.id.image);
            tvName = itemView.findViewById(R.id.name);
            imageClose = itemView.findViewById(R.id.close);
            RecentsModel model = RecentsModel.INSTANCE.get(mContext);
            TaskThumbnailCache thumbnailCache = model.getThumbnailCache();
            TaskIconCache iconCache = model.getIconCache();
            mThumbnailLoadRequest = thumbnailCache.updateThumbnailInBackground(
                      mTask, thumbnail -> imageView.setImageBitmap(thumbnail.thumbnail));

            mIconLoadRequest = iconCache.updateIconInBackground(mTask,
                    (task) -> {
                        setIcon(task.icon);
                    });

        }

        private void setIcon(Drawable icon) {
            if (icon != null) {
                mIconView.setDrawable(icon);
            }
        }
    }

}
