package org.swordapp.server;

import java.util.Date;
import org.apache.abdera.Abdera;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Feed;

public class CollectionListManagerImpl
    implements CollectionListManager
{
    public Feed listCollectionContents(IRI collectionIRI, AuthCredentials auth, SwordConfiguration config) 
            throws SwordServerException, SwordAuthException, SwordError
    {
        Abdera abdera = new Abdera();
        Feed feed = abdera.getFactory().newFeed();
        feed.setId("tag:example.org,2007:/foo");
        feed.setTitle("Test Feed");
        feed.setSubtitle("Feed subtitle");
        feed.setUpdated(new Date());
        feed.addAuthor("James Snell");
        feed.addLink("http://example.com");
        return feed;
    }
}
