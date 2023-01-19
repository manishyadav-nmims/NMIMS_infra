package com.nmims.app.Adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nmims.app.Models.DownloadFileDateModel;
import com.nmims.app.R;

import java.io.File;
import java.util.List;

public class DownloadListRecyclerviewAdapter extends RecyclerView.Adapter<DownloadListRecyclerviewAdapter.MyViewHolder>
{
    private Context context;
    private List<DownloadFileDateModel> downloadFileDateModelList;

    public DownloadListRecyclerviewAdapter(Context context, List<DownloadFileDateModel> downloadFileDateModelList)
    {
        this.context = context;
        this.downloadFileDateModelList = downloadFileDateModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.download_file_design, viewGroup, false);

        return new MyViewHolder(itemView, context, downloadFileDateModelList);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i)
    {
        DownloadFileDateModel downloadFileDateModel = downloadFileDateModelList.get(i);
        myViewHolder.fileName.setText(downloadFileDateModel.getFileName());

        if(downloadFileDateModel.getFileType()!= null && !TextUtils.isEmpty(downloadFileDateModel.getFileType()))
        {
            if(downloadFileDateModel.getFileType().equals(".jpg") || downloadFileDateModel.getFileType().equals(".jpeg") ||  downloadFileDateModel.getFileType().equals(".png"))
            {
                myViewHolder.fileType.setImageDrawable(context.getResources().getDrawable(R.drawable.img_file));
            }

            else if(downloadFileDateModel.getFileType().equals(".doc") || downloadFileDateModel.getFileType().equals(".docx"))
            {
                myViewHolder.fileType.setImageDrawable(context.getResources().getDrawable(R.drawable.word_file));
            }

            else if(downloadFileDateModel.getFileType().equals(".ppt") || downloadFileDateModel.getFileType().equals(".pptx"))
            {
                myViewHolder.fileType.setImageDrawable(context.getResources().getDrawable(R.drawable.ppt_file));
            }

            else if(downloadFileDateModel.getFileType().equals(".pdf"))
            {
                myViewHolder.fileType.setImageDrawable(context.getResources().getDrawable(R.drawable.pdf_file));
            }

            else if(downloadFileDateModel.getFileType().equals(".txt"))
            {
                myViewHolder.fileType.setImageDrawable(context.getResources().getDrawable(R.drawable.txt_file));
            }

            else if(downloadFileDateModel.getFileType().equals(".gif"))
            {
                myViewHolder.fileType.setImageDrawable(context.getResources().getDrawable(R.drawable.gif));
            }

            else if(downloadFileDateModel.getFileType().equals(".rar"))
            {
                myViewHolder.fileType.setImageDrawable(context.getResources().getDrawable(R.drawable.rar_file));
            }

            else if(downloadFileDateModel.getFileType().equals(".zip"))
            {
                myViewHolder.fileType.setImageDrawable(context.getResources().getDrawable(R.drawable.zip_file));
            }

            else if(downloadFileDateModel.getFileType().equals(".htm") || downloadFileDateModel.getFileType().equals(".html"))
            {
                myViewHolder.fileType.setImageDrawable(context.getResources().getDrawable(R.drawable.html_file));
            }

            else if(downloadFileDateModel.getFileType().equals(".mp3"))
            {
                myViewHolder.fileType.setImageDrawable(context.getResources().getDrawable(R.drawable.mp3));
            }

            else if(downloadFileDateModel.getFileType().equals(".mp4") || downloadFileDateModel.getFileType().equals(".mov"))
            {
                myViewHolder.fileType.setImageDrawable(context.getResources().getDrawable(R.drawable.video_file));
            }

            else
            {
                myViewHolder.fileType.setImageDrawable(context.getResources().getDrawable(R.drawable.file_file));
            }
        }
    }

    @Override
    public int getItemCount() {
        return downloadFileDateModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private Context context;
        private List<DownloadFileDateModel> downloadFileDateModelList;
        private ImageView fileType;
        private TextView fileName;


        public MyViewHolder(@NonNull View itemView,Context context,List<DownloadFileDateModel> downloadFileDateModelList)
        {
            super(itemView);
            this.context = context;
            this.downloadFileDateModelList = downloadFileDateModelList;

            fileName = itemView.findViewById(R.id.fileName);
            fileType = itemView.findViewById(R.id.fileType);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        {
            if(v.getId() == itemView.getId())
            {
                File file = downloadFileDateModelList.get(getAdapterPosition()).getFile();
                openFile(file);
            }
        }
    }

    private void openFile(File url) {

        try
        {

            Uri uri = Uri.fromFile(url);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (url.toString().contains(".doc") || url.toString().contains(".docx"))
            {
                // Word document
                intent.setDataAndType(uri, "application/msword");
            }
            else if (url.toString().contains(".pdf"))
            {
                // PDF file
                intent.setDataAndType(uri, "application/pdf");
            }
            else if (url.toString().contains(".ppt") || url.toString().contains(".pptx"))
            {
                // Powerpoint file
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
            }

            else if (url.toString().contains(".htm") || url.toString().contains(".html"))
            {
                // Html file
                intent.setDataAndType(uri,  "text/html");
            }

            else if (url.toString().contains(".xls") || url.toString().contains(".xlsx"))
            {
                // Excel file
                intent.setDataAndType(uri, "application/vnd.ms-excel");
            }
            else if (url.toString().contains(".zip"))
            {
                // ZIP file
                intent.setDataAndType(uri, "application/zip");
            }
            else if (url.toString().contains(".rar"))
            {
                // RAR file
                intent.setDataAndType(uri, "application/x-rar-compressed");
            }
            else if (url.toString().contains(".rtf"))
            {
                // RTF file
                intent.setDataAndType(uri, "application/rtf");
            }
            else if (url.toString().contains(".wav") || url.toString().contains(".mp3"))
            {
                // WAV audio file
                intent.setDataAndType(uri, "audio/x-wav");
            }
            else if (url.toString().contains(".gif"))
            {
                // GIF file
                intent.setDataAndType(uri, "image/gif");
            }
            else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png"))
            {
                // JPG file
                intent.setDataAndType(uri, "image/jpeg");
            }
            else if (url.toString().contains(".txt"))
            {
                // Text file
                intent.setDataAndType(uri, "text/plain");
            }
            else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") ||
                    url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi"))
            {
                // Video files
                intent.setDataAndType(uri, "video/*");
            }
            else
            {
                intent.setDataAndType(uri, "*/*");
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        catch (ActivityNotFoundException e)
        {
            Toast.makeText(context, "No application found which can open the file", Toast.LENGTH_SHORT).show();
        }
    }
}
