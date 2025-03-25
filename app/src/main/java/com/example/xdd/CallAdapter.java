package com.example.xdd;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CallAdapter extends RecyclerView.Adapter<CallAdapter.CallViewHolder> {

    private List<Call> callList;

    public CallAdapter(List<Call> callList) {
        this.callList = callList;
    }

    @NonNull
    @Override
    public CallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_call, parent, false);
        return new CallViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CallViewHolder holder, int position) {
        Call call = callList.get(position);
        holder.txtCallName.setText(call.getCallName());
        holder.txtCallStatus.setText(call.getCallStatus());
    }

    @Override
    public int getItemCount() {
        return callList.size();
    }

    public void updateCalls(List<Call> newCallList) {
        callList = newCallList;
        notifyDataSetChanged();
    }

    static class CallViewHolder extends RecyclerView.ViewHolder {
        TextView txtCallName, txtCallStatus;

        public CallViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCallName = itemView.findViewById(R.id.txt_call_name);
            txtCallStatus = itemView.findViewById(R.id.txt_call_status);
        }
    }
}