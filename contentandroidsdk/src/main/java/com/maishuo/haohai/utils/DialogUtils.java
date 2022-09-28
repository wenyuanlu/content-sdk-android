package com.maishuo.haohai.utils;

import androidx.appcompat.app.AppCompatActivity;

import com.maishuo.haohai.R;
import com.maishuo.haohai.api.bean.DialogBottomMoreBean;
import com.maishuo.haohai.common.dialog.CommonBottomListDialog;
import com.maishuo.haohai.common.dialog.CommonDialog;
import com.maishuo.haohai.listener.OnCommonBottomDialogItemClickListener;
import com.maishuo.haohai.listener.OnDialogCallBackListener;

import java.util.List;

public class DialogUtils {

    /**
     * 展示通用的dialog
     * 通用参数
     */
    public static void showCommonDialog(AppCompatActivity activity, String content, OnDialogCallBackListener listener) {
        showCommonDialog(activity, "提示", content, "取消", "确定", true, true, true, listener);
    }

    public static void showCommonDialog(AppCompatActivity activity, String title, String content, OnDialogCallBackListener listener) {
        showCommonDialog(activity, title, content, true, listener);
    }

    public static void showCommonDialog(AppCompatActivity activity, String title, String content, boolean isCancel, OnDialogCallBackListener listener) {
        showCommonDialog(activity, title, content, "取消", "确定", true, true, isCancel, listener);
    }

    public static void showCommonRightDialog(AppCompatActivity activity, String title, String content, OnDialogCallBackListener listener) {
        showCommonDialog(activity, title, content, "", "知道了", false, true, true, listener);
    }

    public static void showCommonCustomDialog(
            AppCompatActivity activity,
            String title,
            String content,
            OnDialogCallBackListener listener) {
        showCommonDialog(
                activity,
                title,
                content,
                activity.getString(R.string.refuse),
                activity.getString(R.string.agree),
                true,
                true,
                false,
                listener);
    }

    public static void showCommonDialog(
            AppCompatActivity activity,
            String title,
            String content,
            String leftText,
            String rightText,
            OnDialogCallBackListener listener) {
        showCommonDialog(
                activity,
                title,
                content,
                leftText,
                rightText,
                true,
                true,
                false,
                listener);
    }

    public static void showCommonDialog(
            AppCompatActivity activity,
            String title,
            String content,
            String leftText,
            String rightText,
            boolean isCancel,
            OnDialogCallBackListener listener) {
        showCommonDialog(
                activity,
                title,
                content,
                leftText,
                rightText,
                true,
                true,
                isCancel,
                listener);
    }

    /**
     * 展示通用的dialog
     * 全部参数
     */
    public static void showCommonDialog(AppCompatActivity activity, String title, String content, String leftTitle, String rightTitle,
                                        boolean showLeft, boolean showRight, boolean isCancel, OnDialogCallBackListener listener) {

        if (Utils.isDestroy(activity)) {
            return;
        }

        CommonDialog commonDialog = new CommonDialog(activity);
        commonDialog.setTitle(title);
        commonDialog.setContent(content);
        commonDialog.setShowLeft(showLeft);
        commonDialog.setShowRight(showRight);
        commonDialog.setLeftText(leftTitle);
        commonDialog.setRightText(rightTitle);
        commonDialog.setCancelable(isCancel);
        commonDialog.setOnDialogBackListener(listener);
        commonDialog.showDialog();
    }

//    /**
//     * 展示通用的,包含编辑框的dialog
//     * 全部参数
//     */
//    public static void showCommonEditDialog (AppCompatActivity activity, String title, String hintContent, String leftTitle, String rightTitle,
//                                             boolean showLeft, boolean showRight, boolean isCancel, int maxNumber, OnDialogCallBackListener listener) {
//        if (Utils.isDestroy(activity)) {
//            return;
//        }
//
//        CommonEditDialog commonEditDialog = new CommonEditDialog(activity);
//        commonEditDialog.setTitle(title);
//        commonEditDialog.setHintContent(hintContent);
//        commonEditDialog.setShowLeft(showLeft);
//        commonEditDialog.setShowRight(showRight);
//        commonEditDialog.setLeftText(leftTitle);
//        commonEditDialog.setRightText(rightTitle);
//        commonEditDialog.setCancelable(isCancel);
//        commonEditDialog.setMaxNumber(maxNumber);
//        commonEditDialog.setOnDialogBackListener(listener);
//        commonEditDialog.showDialog();
//    }

//    /**
//     * 展示设置代理的dialog
//     */
//    public static void showProxyDialog (
//            AppCompatActivity activity,
//            String title,
//            String subTitle,
//            String sub2Title,
//            String content,
//            OnProxyDialogBackListener listener) {
//        if (Utils.isDestroy(activity)) {
//            return;
//        }
//
//        CommonSettingProxyDialog commonSettingProxyDialog = new CommonSettingProxyDialog(activity);
//        commonSettingProxyDialog.setEnvironment(title);
//        commonSettingProxyDialog.setVersion(subTitle);
//        commonSettingProxyDialog.setChannel(sub2Title);
//        commonSettingProxyDialog.setContent(content);
//        commonSettingProxyDialog.setCancelable(false);
//        commonSettingProxyDialog.setOnProxyDialogBackListener(listener);
//        commonSettingProxyDialog.showDialog();
//    }

//    /**
//     * 展示聊天举报的dialog
//     */
//    public static void showReportDialog (final AppCompatActivity activity, OnDialogReportListener listener) {
//        List<DialogBottomMoreBean> dataList = new ArrayList<>();
//        dataList.add(new DialogBottomMoreBean(-1, "举报"));
//        showBottomMoreDialog(activity, dataList, (itemView, position, dialog) -> {
//            if (listener != null) {
//                listener.onReport();
//            }
//            dialog.dismiss();
//        });
//    }
//

    /**
     * 底部弹出的列表弹窗
     */
    public static void showBottomMoreDialog(final AppCompatActivity activity, List<DialogBottomMoreBean> dataList, OnCommonBottomDialogItemClickListener listener) {
        CommonBottomListDialog commonBottomListDialog = new CommonBottomListDialog(activity);
        commonBottomListDialog.showDialog();

        commonBottomListDialog.setDataList(dataList);
        commonBottomListDialog.setOnCommonBottomDialogItemClick(listener);
    }

//    /**
//     * 底部弹出的举报列表的弹窗
//     */
//    public static void showReportMoreDialog (final AppCompatActivity activity, String otherUserId) {
//        //举报二级弹窗
//        List<DialogBottomMoreBean> dataList = new ArrayList<>();
//        dataList.add(new DialogBottomMoreBean(27, "政治，色情等敏感信息"));
//        dataList.add(new DialogBottomMoreBean(28, "散播虚假广告"));
//        dataList.add(new DialogBottomMoreBean(530, "谩骂，诽谤他人"));
//        dataList.add(new DialogBottomMoreBean(531, "诈骗，诱导"));
//        dataList.add(new DialogBottomMoreBean(532, "其他原因"));
//        showBottomMoreDialog(activity, dataList, (itemView, position, dialog) -> {
//            DialogBottomMoreBean bean       = dataList.get(position);
//            int                  reportType = bean.getReportType();
//            String               text       = bean.getText();
//            if (reportType == 27 || reportType == 28 || reportType == 530 || reportType == 531) {
//                dialog.dismiss();
//                showCommonDialog(activity, "举报内容" + text,
//                        "虚假举报将受到惩罚哦",
//                        "取消", "确定举报",
//                        true, true,
//                        true, new OnDialogCallBackListener() {
//                            @Override
//                            public void onSure (String content) {
//                                ReportApiParam reportApiParam = new ReportApiParam();
//                                reportApiParam.setReportType(reportType);
//                                reportApiParam.setContent(content);
//                                reportApiParam.setCircleId("");
//                                reportApiParam.setCommentId("");
//                                reportApiParam.setVoiceId("");
//                                reportApiParam.setFriendId(otherUserId);
//                                reportApiParam.setCloseFriendId("");
//                                ApiService.Companion.getInstance().reportApi(reportApiParam)
//                                        .subscribe(new CommonObserver<String>() {
//                                            @Override
//                                            public void onResponseSuccess (@Nullable String response) {
//                                                ToastUtil.showToast("举报成功");
//                                            }
//                                        });
//                            }
//
//                            @Override
//                            public void onCancel () {
//                            }
//                        });
//            } else if (reportType == 532) {
//                dialog.dismiss();
//                showReportOtherDialog(activity, new OnDialogCallBackListener() {
//                    @Override
//                    public void onSure (String content) {
//                        ReportApiParam reportApiParam = new ReportApiParam();
//                        reportApiParam.setReportType(reportType);
//                        reportApiParam.setContent(content);
//                        reportApiParam.setCircleId(otherUserId);
//                        reportApiParam.setCommentId("");
//                        reportApiParam.setVoiceId("");
//                        reportApiParam.setFriendId("");
//                        reportApiParam.setCloseFriendId("");
//                        ApiService.Companion.getInstance().reportApi(reportApiParam)
//                                .subscribe(new CommonObserver<String>() {
//                                    @Override
//                                    public void onResponseSuccess (@Nullable String response) {
//                                        ToastUtil.showToast("举报成功");
//                                    }
//                                });
//                    }
//
//                    @Override
//                    public void onCancel () {
//
//                    }
//                });
//            }
//        });
//    }
//
//    /**
//     * 举报的其他的弹窗
//     */
//    public static void showReportOtherDialog (final AppCompatActivity activity, OnDialogCallBackListener listener) {
//        showCommonEditDialog(activity, "其他原因", "请输入您的举报原因，我们会尽快处理，谢谢！",
//                "取消", "确定", true, true, true, 99, listener);
//    }
//
//    /**
//     * 底部弹出的定时弹窗
//     */
//    public static void showTimingDialog (final AppCompatActivity activity, OnAutoCloseListener onAutoCloseListener) {
//        long                       time               = PreferencesUtils.getLong(PreferencesKey.TIMING);
//        final AutoClosePlayEnum[]  autoClosePlayEnums = AutoClosePlayEnum.values();
//        List<DialogBottomMoreBean> dataList           = new ArrayList<>();
//
//        for (int i = 0; i < autoClosePlayEnums.length; i++) {
//            AutoClosePlayEnum    autoClosePlayEnum = autoClosePlayEnums[i];
//            boolean              isSelector        = (i == 0 && -1 == time) || autoClosePlayEnum.getValue() == time;
//            DialogBottomMoreBean bean              = new DialogBottomMoreBean(autoClosePlayEnum.getType(), autoClosePlayEnum.getDesc(), isSelector, 3);
//            dataList.add(bean);
//        }
//
//        showBottomMoreDialog(activity, dataList, (itemView, position, dialog) -> {
//            AutoClosePlayEnum bean = autoClosePlayEnums[position];
//            PreferencesUtils.putLong(PreferencesKey.TIMING, bean.getValue());
//
//            if (onAutoCloseListener != null) {
//                onAutoCloseListener.onClose(bean);
//            }
//            dialog.dismiss();
//        });
//    }
//
//    /**
//     * 底部弹出的播放模式
//     */
//    public static void showPlayTypeDialog (final AppCompatActivity activity) {
//        //播放模式弹窗 0随机播放 1循环播放
//        int                  selectType = PreferencesUtils.getInt(PreferencesKey.PLAY_TYPE, 0);
//        DialogBottomMoreBean bean1      = new DialogBottomMoreBean(0, "随机播放", true, 2);
//        DialogBottomMoreBean bean2      = new DialogBottomMoreBean(1, "循环播放", false, 2);
//        if (selectType == 1) {
//            bean1.setSelect(false);
//            bean2.setSelect(true);
//        }
//
//        List<DialogBottomMoreBean> dataList = new ArrayList<>();
//        dataList.add(bean1);
//        dataList.add(bean2);
//        showBottomMoreDialog(activity, dataList, (itemView, position, dialog) -> {
//            DialogBottomMoreBean bean       = dataList.get(position);
//            int                  reportType = bean.getReportType();
//            if (reportType == 0) {
//                //随机播放
//                PreferencesUtils.putInt(PreferencesKey.PLAY_TYPE, reportType);
//                dialog.dismiss();
//            } else if (reportType == 1) {
//                //循环播放
//                PreferencesUtils.putInt(PreferencesKey.PLAY_TYPE, reportType);
//                dialog.dismiss();
//            }
//        });
//    }
//
//    /**
//     * 底部弹出性别选择
//     */
//    public static void showSexSelectDialog (final AppCompatActivity activity, String sex,
//                                            SelectCallBack selectCallBack) {
//        DialogBottomMoreBean bean1 = new DialogBottomMoreBean(1, "男", true, 2);
//        DialogBottomMoreBean bean2 = new DialogBottomMoreBean(2, "女", false, 2);
//        if (sex.equals("女")) {
//            bean1.setSelect(false);
//            bean2.setSelect(true);
//        }
//        List<DialogBottomMoreBean> dataList = new ArrayList<>();
//        dataList.add(bean1);
//        dataList.add(bean2);
//        showBottomMoreDialog(activity, dataList, (itemView, position, dialog) -> {
//            DialogBottomMoreBean bean = dataList.get(position);
//            selectCallBack.onCallBack(bean);
//            dialog.dismiss();
//        });
//    }
//
//    public interface SelectCallBack {
//        void onCallBack (DialogBottomMoreBean bottomMoreBean);
//    }
}
