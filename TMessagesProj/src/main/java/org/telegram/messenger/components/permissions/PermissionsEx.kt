package org.telegram.messenger.components.permissions

import android.app.Activity
import android.content.Context
import android.provider.Settings
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment


/**
 * Created by Siddikov Mukhriddin on 5/20/22
 */
fun Fragment.checkPermission(permission: String, granted: () -> Unit) {
    val mContext = context ?: return
    val options = Permissions.Options()
    options.setSettingsDialogMessage("Вы навсегда запретили приложению доступ к геопозиции! Разрешите приложению использовать ваши геоданные в настройках!")
    options.setSettingsText("Настройки")
    options.setSettingsDialogTitle("Внимание!")
    options.setCreateNewTask(true)
    Permissions.check(mContext, arrayOf(permission), null, options, object : PermissionHandler() {
        override fun onGranted() {
            granted()
        }
    })
}

fun checkPermission(context: Context, permission: String, granted: () -> Unit, denied: () -> Unit) {
    val mContext = context ?: return
    val options =  Permissions.Options()
//    val options = Permissions.Options()
    options.setSettingsDialogMessage("Вы навсегда запретили приложению доступ к хранилишу! Разрешите приложению использовать ваши Файлы и медиа в настройках!")
    options.setSettingsText("Настройки")
    options.setSettingsDialogTitle("Внимание!")
    options.setCreateNewTask(true)
    Permissions.check(mContext, arrayOf(permission), null, options, object : PermissionHandler() {
        override fun onGranted() {
            granted()
        }

        override fun onDenied(context: Context?, deniedPermissions: java.util.ArrayList<String>?) {
            denied()
        }

        override fun onBlocked(
            context: Context?,
            blockedList: java.util.ArrayList<String>?
        ): Boolean {
            denied()
            return super.onBlocked(context, blockedList)
        }

    })
}




fun checkPermissionLocation(context: Context?,permission: String, denied: () -> Unit, granted: () -> Unit) {
    val mContext = context ?: return
    val options = Permissions.Options()
//    val options = Permissions.Options()
    options.setSettingsDialogMessage("You have permanently blocked the application from accessing your location! Allow the application to use your geodata!\nThe \"Islamic Telegram\" application collects location data to ensure the functionality of the application (for the monitoring center)")
    options.setSettingsText("Settings")
    options.setSettingsDialogTitle("Attention!")
    options.setCreateNewTask(true)
    options.sendDontAskAgainToSettings(true)
    options.sendBlockedToSettings = true
    Permissions.check(mContext, arrayOf(permission), null, options, object : PermissionHandler() {
        override fun onGranted() {
            granted()
        }
        override fun onDenied(context: Context?, deniedPermissions: java.util.ArrayList<String>?) {
            denied()
        }

        override fun onBlocked(
            context: Context?,
            blockedList: java.util.ArrayList<String>?,
        ): Boolean {
            denied()
            return super.onBlocked(context, blockedList)
        }

    })
}

fun Activity.openSettings(){
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = Uri.fromParts("package", this.packageName, null)
    startActivity(intent)
}
