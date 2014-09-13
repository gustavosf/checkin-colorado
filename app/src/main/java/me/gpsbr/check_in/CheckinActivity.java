package me.gpsbr.check_in;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Controller da atividade "Checkin"
 * Esta atividade é mostrada logo após o login, tem como objetivo mostrar uma lista de partidas
 * cujo checkin está aberto. Normalmente, o clube abre apenas uma partida por vês mas o controller
 * prevê a possibilidade da abertura de checkin para mais de uma partida.
 *
 * @author   Gustavo Seganfredo <gustavosf@gmail.com>
 * @since    1.0
 */
public class CheckinActivity extends Activity {

    public final static String EXTRA_GAME_ID = "me.gpsbr.checkin.GAME_ID";
    public final static String EXTRA_EVENT_ID = "me.gpsbr.checkin.EVENT_ID";

    // UI Refs
    protected ListView mGameList;
    protected TextView mCheckinClosedMessage;

    // Data refs
    private List<Game> games;

    // ------------------------------------------------------------------------------------- //
    // - Métodos da Atividade -------------------------------------------------------------- //
    // ------------------------------------------------------------------------------------- //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin);

        // Inicialização das referências de UI
        mGameList = (ListView) findViewById(R.id.game_list);
        mCheckinClosedMessage = (TextView) findViewById(R.id.checkin_closed_message);

        games = App.getGameList();

        if (games.isEmpty()) {
            // Checkin fechado, esconde a lista de jogos e mostra mensagem
            mCheckinClosedMessage.setVisibility(View.VISIBLE);
            mGameList.setVisibility(View.GONE);
        } else {
            // Monta lista de jogos
            ArrayAdapter<Game> adapter = new GameListAdapter();
            mGameList.setAdapter(adapter);
            registerClickCallback();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.checkin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            App.logout();
            finish();
        } else if (id == R.id.action_about) {
            App.showAbout(this);
        } else if (id == R.id.action_notify) {
            int notificationId = 001;
            // Build intent for notification content
            Intent viewIntent = new Intent(this, CheckinActivity.class);
            viewIntent.putExtra(EXTRA_EVENT_ID, "0001");
            PendingIntent viewPendingIntent =
                    PendingIntent.getActivity(this, 0, viewIntent, 0);

            // Build an intent for an action to view a map
            Intent mapIntent = new Intent(Intent.ACTION_VIEW);
            PendingIntent mapPendingIntent =
                    PendingIntent.getActivity(this, 0, mapIntent, 0);

            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_launcher)
                            .setLargeIcon(BitmapFactory.decodeResource(
                                    getResources(), R.drawable.inter_x_botafogo))
                            // .setContentTitle("Check-in")
                            .setContentText("Check-in aberto para Inter x Botafogo!")
                            .setContentIntent(viewPendingIntent)
                            .addAction(R.drawable.ic_launcher,
                                "Fazer check-in", mapPendingIntent);

            // Get an instance of the NotificationManager service
            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(this);

            // Build the notification and issues it with notification manager.
            notificationManager.notify(notificationId, notificationBuilder.build());
        }
        return super.onOptionsItemSelected(item);
    }

    // ------------------------------------------------------------------------------------- //
    // - Outros Métodos -------------------------------------------------------------------- //
    // ------------------------------------------------------------------------------------- //

    /**
     * Callback para quando se clica em um jogo da lista
     */
    private void registerClickCallback() {
        ListView list = (ListView)findViewById(R.id.game_list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long id) {
                Intent intent = new Intent(CheckinActivity.this, CheckinGameActivity.class);
                intent.putExtra(EXTRA_GAME_ID, position);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
            }
        });
    }

    /**
     * Adapter para mostrar os jogos no formato de lista
     */
    private class GameListAdapter extends ArrayAdapter<Game> {
        public GameListAdapter() {
            super(CheckinActivity.this, R.layout.game_list_view, games);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.game_list_view, parent, false);
            }

            Game currentGame = games.get(position);

            TextView tv;
            tv = (TextView)itemView.findViewById(R.id.game_tournament);
            tv.setText(currentGame.getTournament());
            tv = (TextView)itemView.findViewById(R.id.game_players);
            tv.setText(currentGame.getHome()+" x "+currentGame.getAway());
            tv = (TextView)itemView.findViewById(R.id.game_date);
            tv.setText(currentGame.getDate());
            tv = (TextView)itemView.findViewById(R.id.game_venue);
            tv.setText(currentGame.getVenue());

            return itemView;
        }
    }
}
