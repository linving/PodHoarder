package com.podhoarder.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.podhoarder.object.PaletteTransformation;
import com.podhoarder.object.SearchResultRow;
import com.podhoarder.util.DataParser;
import com.podhoarder.util.LetterTileProvider;
import com.podhoarderproject.podhoarder.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.RecyclerView.Adapter;
import static android.support.v7.widget.RecyclerView.ViewHolder;

public class SearchResultsAdapter extends Adapter
{
	@SuppressWarnings("unused")
	private static final 	String 								LOG_TAG = "com.podhoarder.adapter.SearchResultsAdapter";
	private 				List<SearchResultRow> 				results;
	private 				Context 							context;
    private final           LetterTileProvider                  mTileProvider;

    private                 OnSubscribeListener                 mOnSubscribeListener;

	/**
	 * Creates a LatestEpisodesListAdapter (Constructor).
	 *
	 * @param context
	 *            A Context object from the parent Activity.
	 *            
	 */
	public SearchResultsAdapter(Context context)
	{
		this.results = new ArrayList<SearchResultRow>();
		this.context = context;
        this.mTileProvider = new LetterTileProvider(context);
	}
	
	/**
	 * Replaces the item collection behind the adapter to force an update.
	 * @param newItemCollection The new collection.
	 */
	public void replaceItems(List<SearchResultRow> newItemCollection)
	{
		this.results.clear();
		this.results.addAll(newItemCollection);
	}

    @Override
    public SearchResultsAdapterViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cards_layout, parent, false);
        //TODO: Set OnClickListener
        SearchResultsAdapterViewHolder holder = new SearchResultsAdapterViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        SearchResultsAdapterViewHolder holder = (SearchResultsAdapterViewHolder) viewHolder;

        final SearchResultRow currentResult = this.results.get(i);

        final ImageView feedImage = holder.feedImage;

        final LinearLayout titleContainer = holder.titleContainer;
        final TextView feedTitle = holder.feedTitle;
        final TextView feedAuthor = holder.feedAuthor;

        final TextView feedDescription = holder.feedDescription;
        final TextView lastUpdated = holder.lastUpdated;

        final Button subscribeButton = holder.subscribeButton;

        feedTitle.setText(currentResult.getTitle());
        feedAuthor.setText(currentResult.getAuthor());
        lastUpdated.setText(currentResult.getTitle());

        if (currentResult == null || !currentResult.getDescription().isEmpty())
            feedDescription.setText(Html.fromHtml(currentResult.getDescription()).toString());
        else
            feedDescription.setText(this.context.getString(R.string.add_list_feed_no_description));

        try
        {
            lastUpdated.setText(context.getString(R.string.add_list_feed_last_updated) + " " +
                    DateUtils.getRelativeTimeSpanString(
                            DataParser.correctFormat.parse(currentResult.getLastUpdated()).getTime()));	//Set a time stamp since Episode publication.
        }
        catch (ParseException e)
        {
            lastUpdated.setText(context.getString(R.string.add_list_feed_last_updated) + " " + context.getString(R.string.add_list_feed_last_updated_unknown));	//Set a time stamp since Episode publication.
        }
        catch (NullPointerException ex)
        {
            lastUpdated.setText(context.getString(R.string.add_list_feed_last_updated) + " " + context.getString(R.string.add_list_feed_last_updated_unknown));	//Set a time stamp since Episode publication.
        }
        Picasso.with(context)
                .load(currentResult.getImageUrl())
                .transform(PaletteTransformation.getInstance())
                .error(new BitmapDrawable(context.getResources(), mTileProvider.getLetterTile(currentResult.getTitle(), currentResult.getLink(), feedImage.getMaxWidth(), feedImage.getMaxHeight())))
                .into(feedImage, new Callback.EmptyCallback() {
                    @Override
                    public void onSuccess() {
                        Bitmap bitmap = ((BitmapDrawable) feedImage.getDrawable()).getBitmap();
                        Palette palette = PaletteTransformation.getPalette(bitmap);
                        try {
                            Palette.Swatch s = palette.getVibrantSwatch();
                            titleContainer.setBackgroundColor(s.getRgb());
                            feedTitle.setTextColor(s.getTitleTextColor());
                            feedAuthor.setTextColor(s.getBodyTextColor());
                        } catch (NullPointerException e) {
                            Log.e(LOG_TAG, "NullPointerException on " + currentResult.getTitle() + " when trying to get Swatch from Palette!");
                            e.printStackTrace();
                        }
                    }
                });

        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSubscribeListener != null)
                    mOnSubscribeListener.onSubscribeConfirmed(viewHolder.itemView, i, currentResult);
            }
        });
    }

    @Override
	public long getItemId(int position)
	{
		return position;
	}

    @Override
    public int getItemCount() {
        return results.size();
    }
	
	public void add(SearchResultRow row)
	{
		this.results.add(row);
        this.notifyItemInserted(this.results.size()-1);
	}

    public void remove(int i) {
        this.results.remove(i);
        this.notifyItemRemoved(i);
    }
	
	public void clear()
	{
		this.results.clear();
	}

    public interface OnSubscribeListener {
        public void onSubscribeConfirmed(View v, int i, SearchResultRow resultData);
    }

    public void setOnSubscribeListener(OnSubscribeListener listener) {
        mOnSubscribeListener = listener;
    }

    public OnSubscribeListener getOnSubscribeListener() {
        return mOnSubscribeListener;
    }

    public static class SearchResultsAdapterViewHolder extends ViewHolder
    {
        public TextView feedTitle;
        public TextView feedAuthor;
        public TextView lastUpdated;
        public TextView feedDescription;
        public LinearLayout titleContainer;
        public ImageView feedImage;
        public Button subscribeButton;

        public SearchResultsAdapterViewHolder(View view) {
            super(view);

            feedImage = (ImageView) view.findViewById(R.id.search_list_row_image);

            titleContainer = (LinearLayout) view.findViewById(R.id.search_list_row_text_container);
            feedTitle = (TextView) titleContainer.findViewById(R.id.search_list_row_title);
            feedAuthor = (TextView) titleContainer.findViewById(R.id.search_list_row_author);

            feedDescription = (TextView) view.findViewById(R.id.search_list_row_description);
            lastUpdated = (TextView) view.findViewById(R.id.search_list_row_last_updated);

            subscribeButton = (Button) view.findViewById(R.id.search_list_row_button_subscribe);
        }
    }
}
