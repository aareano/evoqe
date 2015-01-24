package evoqe.com.evoqe.objects;

import java.io.File;
import java.util.Date;
import java.util.List;

import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

/**
 * This class represents a user of the evoqe app.
 * The fields of a user are listed below. This class will mostly be used (probably)
 * to get and set different fields belonging to the current user, the owner of this
 * instance of the app. This instance is saved to disc by virtue of Parse, but 
 * basically it will all be copied here for easier access.
 * 
 * 
 * Buuuuuuut....that seems like a lot of work when Parse already has a ParseUser...
 * to be continued...
 * 
 * 
 * @author Aaron
 */
@SuppressWarnings("unused")
public class User {
    
    // User fields:
    // missing fields: authData, coordinates, ACL
    private String username;
    private String password;
    private boolean emailVerified;
    private int addedCount;
    private int ageMax;
    private int ageMin;
    private String contactEmail;
    private String email;
    private int eventsHostedCount;
    private int eventsSavedCount;
    private String facebookId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String gender;
    private boolean hasUsedPromotionalCode;
    private String hostSubscriptionTags;
    private String invitedBy;
    private boolean isPublicHost;
    private int minutesFromCreationToSignUp;
    private ParseRelation<ParseObject> myEvents;
    private ParseRelation<ParseObject> myInvites;
    private ParseRelation<ParseObject> myJamborees;
    private ParseRelation<ParseObject> myPublicEvents;
    private ParseRelation<ParseObject> ownedInvites;
    private ParseRelation<ParseObject> ownedJamborees;
    private String phoneNumber;
    private File profilePicture;
    private String school;
    private ParseRelation<ParseUser> subscribers;
    private ParseRelation<ParseUser> subscriptions;
    private int subscriptionCount;
    private List<String> tags;
    private String userType;
    private String venueID;
    private Date createdAt;
    private Date updatedAt;
    
    public User() {
        new User(null);
    }
    
    public User(ParseUser user) {
        // copy all the information over to member variables
        
    }
    
    
}
