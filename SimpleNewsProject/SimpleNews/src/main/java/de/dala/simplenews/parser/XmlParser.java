package de.dala.simplenews.parser;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

import de.dala.simplenews.common.Category;
import de.dala.simplenews.common.Feed;
import de.dala.simplenews.common.News;

public class XmlParser {
    private static final String TAG_NEWS = "news";
    private static final String TAG_CATEGORY = "category";
    private static final String TAG_FEED = "feed";

    //--------------------------------------------------------------------------------
    //TAGs and ATTRIBUTEs in xml file
    //--------------------------------------------------------------------------------
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_COLOR = "color";
    private static final String ATTRIBUTE_VISIBLE = "visible";
    private static final String ATTRIBUTE_FEED_TITLE = "title";
    /**
     * TAG for logging *
     */
    private static final String TAG = "XmlParser";

    /**
     * Read and parse res/raw/categories.xml or custom file
     *
     * @return {@link News} obj with all data
     * @throws Exception if categories.xml or custom file is not found or if there are errors on parsing
     */
    public static News readDefaultNewsFile(Context context, int xml) throws XmlPullParserException, IOException {
        News news = null;
        try {
            InputStream is;

            is = context.getResources().openRawResource(xml);
            if (is != null) {

                // Create a new XML Pull Parser.
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(is, null);
                parser.nextTag();

                // Create changelog obj that will contain all data
                news = new News();
                // Parse file
                readNews(parser, news);

                // Close inputstream
                is.close();
            } else {
                Log.d(TAG, "categories.xml not found");
            }
        } catch (XmlPullParserException xpe) {
            Log.d(TAG, "XmlPullParseException while parsing file", xpe);
            throw xpe;
        } catch (IOException ioe) {
            Log.d(TAG, "Error i/o with categories.xml", ioe);
            throw ioe;
        }

        if (news != null) {
            Log.d(TAG, "Process ended. News:" + news);
        }

        return news;
    }

    /**
     * Parse changelog node
     *
     * @param parser
     * @param news
     */
    private static void readNews(XmlPullParser parser, News news) throws XmlPullParserException, IOException {
        if (parser == null || news == null) return;
        // Parse changelog node
        parser.require(XmlPullParser.START_TAG, null, TAG_NEWS);
        Log.d(TAG, "Processing main tag=");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tag = parser.getName();
            Log.d(TAG, "Processing tag=" + tag);
            if (tag.equals(TAG_CATEGORY)) {
                readCategory(parser, news);
            }
        }
    }

    /**
     * Parse changeLogVersion node
     *
     * @param parser
     * @param news
     * @throws Exception
     */
    private static void readCategory(XmlPullParser parser, News news) throws XmlPullParserException, IOException {

        if (parser == null) return;

        parser.require(XmlPullParser.START_TAG, null, TAG_CATEGORY);

        // Read attributes
        String categoryName = parser.getAttributeValue(null, ATTRIBUTE_NAME);
        String color = parser.getAttributeValue(null, ATTRIBUTE_COLOR);
        String visible = parser.getAttributeValue(null, ATTRIBUTE_VISIBLE);
        Category category = new Category();
        try {
            category.setColorId(Color.parseColor(color));
        } catch (IllegalArgumentException e) {
            // Unknown color, try to parse it as integer
            try {
                category.setColorId(Integer.parseInt(color));
            } catch (NumberFormatException ne) {
                // not a number, set default value
                category.setColorId(0);
            }
        }
        category.setName(categoryName);
        if (visible != null) {
            category.setVisible(!"false".equals(visible));
        }

        // Parse nested nodes
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tag = parser.getName();
            Log.d(TAG, "Processing tag=" + tag);

            if (tag.equals(TAG_FEED)) {
                Feed feed = readFeed(parser);
                if (feed != null) {
                    category.getFeeds().add(feed);
                }
            }
        }
        news.getCategories().add(category);
    }

    /**
     * Parse changeLogText node
     *
     * @param parser
     * @throws Exception
     */
    private static Feed readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        Feed feed = null;
        if (parser == null) return null;

        parser.require(XmlPullParser.START_TAG, null, TAG_FEED);

        String tag = parser.getName();
        if (tag.equals(TAG_FEED)) {
            // Read attributes
            String feedTitle = parser.getAttributeValue(null, ATTRIBUTE_FEED_TITLE);
            String feedVisible = parser.getAttributeValue(null, ATTRIBUTE_VISIBLE);

            // Read text
            if (parser.next() == XmlPullParser.TEXT) {
                String feedText = parser.getText();
                if (feedText != null) {
                    feed = new Feed(feedText);
                    if (feedTitle != null) {
                        feed.setTitle(feedTitle);
                    }
                    if (feedVisible != null) {
                        feed.setVisible(!"false".equals(feedVisible));
                    }
                }
                parser.nextTag();
            }

        }
        parser.require(XmlPullParser.END_TAG, null, TAG_FEED);
        return feed;
    }
}