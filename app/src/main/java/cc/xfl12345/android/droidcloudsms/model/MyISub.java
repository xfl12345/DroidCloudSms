package cc.xfl12345.android.droidcloudsms.model;

import android.Manifest;
import android.os.Build;
import android.os.RemoteException;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class MyISub {
    protected SystemServiceBinderHelper serviceBinderHelper;

    protected Method methodGetAllSubInfoList;

    protected Method methodGetAvailableSubscriptionInfoList;

    protected Method methodGetActiveSubInfoCountMax;

    protected Method methodGetSlotIndex;

    protected Method methodGetSubIds;

    protected Method methodGetSubId;

    protected Method methodGetDefaultSmsSubId;

    public SystemServiceBinderHelper getProxyHelper() {
        return serviceBinderHelper;
    }

    public MyISub() throws ReflectiveOperationException, RemoteException {
        serviceBinderHelper = new SystemServiceBinderHelper("isub");

        methodGetAllSubInfoList = serviceBinderHelper.getServiceDeclaredMethod("getAllSubInfoList", String.class, String.class);
        methodGetAvailableSubscriptionInfoList = serviceBinderHelper.getServiceDeclaredMethod("getAvailableSubscriptionInfoList", String.class, String.class);
        methodGetActiveSubInfoCountMax = serviceBinderHelper.getServiceDeclaredMethod("getActiveSubInfoCountMax");

        methodGetSlotIndex = serviceBinderHelper.getServiceDeclaredMethod("getSlotIndex", int.class);
        methodGetSubIds = serviceBinderHelper.getServiceDeclaredMethod("getSubIds", int.class);
        methodGetSubId = serviceBinderHelper.getServiceDeclaredMethod("getSubId", int.class);
        methodGetDefaultSmsSubId = serviceBinderHelper.getServiceDeclaredMethod("getDefaultSmsSubId");

    }

    public List<SubscriptionInfo> getAllSubInfoList() {
        try {
            return (List<SubscriptionInfo>) methodGetAllSubInfoList.invoke(serviceBinderHelper.getServiceInstance(), "android", Manifest.permission.READ_PHONE_STATE);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    public List<SubscriptionInfo> getAvailableSubscriptionInfoList() {
        try {
            return (List<SubscriptionInfo>) methodGetAvailableSubscriptionInfoList.invoke(serviceBinderHelper.getServiceInstance(), "android", Manifest.permission.READ_PHONE_STATE);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    public int getActiveSubInfoCountMax() {
        try {
            return (int) methodGetActiveSubInfoCountMax.invoke(serviceBinderHelper.getServiceInstance());
        } catch (IllegalAccessException | InvocationTargetException e) {
            return 0;
        }
    }

    public int getSlotIndex(int subId) {
        try {
            return (int) methodGetSlotIndex.invoke(serviceBinderHelper.getServiceInstance(), subId);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return 0;
        }
    }

    public int[] getSubIds(int slotIndex) {
        try {
            return (int[]) methodGetSubIds.invoke(serviceBinderHelper.getServiceInstance(), slotIndex);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    public int getSubId(int slotIndex) {
        try {
            return (int) methodGetSubId.invoke(serviceBinderHelper.getServiceInstance(), slotIndex);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return 0;
        }
    }

    public int getDefaultSmsSubId() {
        try {
            return (int) methodGetDefaultSmsSubId.invoke(serviceBinderHelper.getServiceInstance());
        } catch (IllegalAccessException | InvocationTargetException e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return SubscriptionManager.DEFAULT_SUBSCRIPTION_ID;
            } else {
                return -1;
            }
        }
    }





}
