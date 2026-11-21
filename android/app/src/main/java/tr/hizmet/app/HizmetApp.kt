package tr.hizmet.app

import android.app.Application
import tr.hizmet.app.data.ApiClient
import tr.hizmet.app.data.ApiService
import tr.hizmet.app.data.SessionStore

/** Creates the objects that the whole app shares (simple manual dependency injection). */
class HizmetApp : Application() {

    lateinit var session: SessionStore
        private set

    lateinit var api: ApiService
        private set

    override fun onCreate() {
        super.onCreate()
        session = SessionStore(this)
        api = ApiClient.create(session)
    }
}
