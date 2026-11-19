/**
 * Copyright (C) 2015 Sebastian Kappes
 * Copyright (C) 2018 Jonas Lochmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */
package tech.alatcerdas.tvkillindo.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
import android.widget.RemoteViews
import tech.alatcerdas.tvkillindo.*

object UpdateWidget {
    fun updateAllWidgets(context: Context) {
        updateAllWidgets(context, AppWidgetManager.getInstance(context))
    }

    fun updateAllWidgets(context: Context, appWidgetManager: AppWidgetManager) {
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val powerOffIntent = TransmitService.buildIntent(
                TransmitServiceSendRequest(
                        TransmitServiceAction.Off,
                        false,
                        null
                ),
                context
        )
        // API 26+: getForegroundService() is the PendingIntent equivalent of
        // ContextCompat.startForegroundService(); TransmitService calls startForeground()
        // as soon as the request is handled. Older devices (API 24/25) use startService()
        // as before.
        val powerOffPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(context, PendingIntents.WIDGET_POWER_OFF, powerOffIntent, flags)
        } else {
            PendingIntent.getService(context, PendingIntents.WIDGET_POWER_OFF, powerOffIntent, flags)
        }

        val cancelIntent = TransmitService.buildIntent(TransmitServiceCancelRequest, context)
        val cancelPendingIntent = PendingIntent.getService(context, PendingIntents.WIDGET_CANCEL, cancelIntent, flags)

        val isRunning = TransmitService.status.value != null

        for (appWidgetId in Settings.with(context).getAppWidgetIds()) {
            if (isRunning) {
                val views = RemoteViews(context.packageName, R.layout.off_widget_running)

                views.setOnClickPendingIntent(R.id.killWidget, cancelPendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } else {
                val views = RemoteViews(context.packageName, R.layout.off_widget)
                views.setOnClickPendingIntent(R.id.killWidget, powerOffPendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}