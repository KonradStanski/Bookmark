package com.example.bookmark.server;

import android.net.Uri;
import android.util.Log;

import com.example.bookmark.models.Book;
import com.example.bookmark.models.EntityId;
import com.example.bookmark.models.Photograph;
import com.example.bookmark.models.Request;
import com.example.bookmark.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * An implementation of StorageProvider that stores all of the app's data in Firebase.
 *
 * @author Kyle Hennig.
 */
public class FirebaseStorageService implements StorageService {
    public interface FirestoreDeserializer<T> {
        T deserialize(String id, Map<String, Object> map);
    }

    protected enum Collection {
        USERS, BOOKS, REQUESTS, PHOTOGRAPHS
    }

    private static final String TAG = "FirebaseStorageService";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    public void storeUser(User user, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        storeEntity(Collection.USERS, user, onSuccessListener, onFailureListener);
    }

    @Override
    public void retrieveUserByUsername(String username, OnSuccessListener<User> onSuccessListener, OnFailureListener onFailureListener) {
        retrieveEntity(Collection.USERS, username, User::fromFirestoreDocument, onSuccessListener, onFailureListener);
    }

    @Override
    public void storeBook(Book book, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        storeEntity(Collection.BOOKS, book, onSuccessListener, onFailureListener);
    }

    @Override
    public void retrieveBook(EntityId id, OnSuccessListener<Book> onSuccessListener, OnFailureListener onFailureListener) {
        retrieveEntity(Collection.BOOKS, id.toString(), Book::fromFirestoreDocument, onSuccessListener, onFailureListener);
    }

    @Override
    public void retrieveBooks(OnSuccessListener<List<Book>> onSuccessListener, OnFailureListener onFailureListener) {
        retrieveEntities(Collection.BOOKS, Book::fromFirestoreDocument, onSuccessListener, onFailureListener);
    }

    @Override
    public void retrieveBooksByOwner(User owner, OnSuccessListener<List<Book>> onSuccessListener, OnFailureListener onFailureListener) {
        retrieveEntitiesMatching(Collection.BOOKS, query -> query.whereEqualTo("ownerId", owner.getId().toString()), Book::fromFirestoreDocument, onSuccessListener, onFailureListener);
    }

    @Override
    public void retrieveBooksByRequester(User requester, OnSuccessListener<List<Book>> onSuccessListener, OnFailureListener onFailureListener) {
        retrieveRequestsByRequester(requester, requests -> {
            List<EntityId> bookIds = new ArrayList<>();
            for (Request request : requests) {
                bookIds.add(request.getBookId());
            }
            retrieveBooks(books -> {
                List<Book> booksByRequester = new ArrayList<>();
                for (Book book : books) {
                    if (bookIds.contains(book.getId())) {
                        booksByRequester.add(book);
                    }
                }
                onSuccessListener.onSuccess(booksByRequester);
            }, onFailureListener);
        }, onFailureListener);
    }

    @Override
    public void deleteBook(Book book, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        deleteEntity(Collection.BOOKS, book.getId().toString(), onSuccessListener, onFailureListener);
    }

    @Override
    public void storeRequest(Request request, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        storeEntity(Collection.REQUESTS, request, onSuccessListener, onFailureListener);
    }

    @Override
    public void retrieveRequest(EntityId id, OnSuccessListener<Request> onSuccessListener, OnFailureListener onFailureListener) {
        retrieveEntity(Collection.REQUESTS, id.toString(), Request::fromFirestoreDocument, onSuccessListener, onFailureListener);
    }

    @Override
    public void retrieveRequestsByBook(Book book, OnSuccessListener<List<Request>> onSuccessListener, OnFailureListener onFailureListener) {
        retrieveEntitiesMatching(Collection.REQUESTS, query -> query.whereEqualTo("bookId", book.getId().toString()), Request::fromFirestoreDocument, onSuccessListener, onFailureListener);
    }

    @Override
    public void retrieveRequestsByRequester(User requester, OnSuccessListener<List<Request>> onSuccessListener, OnFailureListener onFailureListener) {
        retrieveEntitiesMatching(Collection.REQUESTS, query -> query.whereEqualTo("requesterId", requester.getId().toString()), Request::fromFirestoreDocument, onSuccessListener, onFailureListener);
    }

    @Override
    public void deleteRequest(Request request, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        deleteEntity(Collection.REQUESTS, request.getId().toString(), onSuccessListener, onFailureListener);
    }

    @Override
    public void storePhotograph(Photograph photograph, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        String photographPath = String.format("%s/%s", getCollectionName(Collection.PHOTOGRAPHS), photograph.getId().toString());
        StorageReference imageReference = storage.getReference().child(photographPath);
        imageReference.putFile(photograph.getImageUri()).addOnSuccessListener(taskSnapshot -> {
            Log.d(TAG, String.format("Stored photograph with id %s.", photograph.getId()));
            onSuccessListener.onSuccess(null);
        }).addOnFailureListener(e -> {
            Log.w(TAG, String.format("Error storing photograph with id %s: ", photograph.getId()), e);
            onFailureListener.onFailure(e);
        });
    }

    @Override
    public void retrievePhotograph(EntityId id, OnSuccessListener<Photograph> onSuccessListener, OnFailureListener onFailureListener) {
        String photographPath = String.format("%s/%s", getCollectionName(Collection.PHOTOGRAPHS), id.toString());
        StorageReference imageReference = storage.getReference().child(photographPath);
        imageReference.getDownloadUrl().addOnSuccessListener(uri -> {
            try {
                File file = File.createTempFile(id.toString(), "temp");
                imageReference.getFile(file).addOnSuccessListener(taskSnapshot -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("imageUri", Uri.fromFile(file).toString());
                    Photograph photograph = Photograph.fromFirestoreDocument(id.toString(), map);
                    Log.d(TAG, String.format("Retrieved photograph with id %s.", id.toString()));
                    onSuccessListener.onSuccess(photograph);
                }).addOnFailureListener(e -> {
                    Log.w(TAG, String.format("Error retrieving photograph with id %s: ", id.toString()), e);
                    onFailureListener.onFailure(e);
                });
            } catch (IOException e) {
                Log.d(TAG, String.format("Failed to create a local file to store the photograph with id %s: ", id.toString()), e);
                onFailureListener.onFailure(e);
            }
        }).addOnFailureListener(e -> {
            if (e instanceof StorageException) {
                // No photograph with the id exists. Returns null to be consistent.
                onSuccessListener.onSuccess(null);
                return;
            }
            Log.w(TAG, String.format("Error retrieving URI of photograph with id %s: ", id.toString()), e);
            onFailureListener.onFailure(e);
        });
    }

    @Override
    public void deletePhotograph(Photograph photograph, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        String photographPath = String.format("%s/%s", getCollectionName(Collection.PHOTOGRAPHS), photograph.getId().toString());
        StorageReference imageReference = storage.getReference().child(photographPath);
        imageReference.delete().addOnSuccessListener(aVoid -> {
            Log.d(TAG, String.format("Deleted photograph with id %s.", photograph.getId().toString()));
            onSuccessListener.onSuccess(null);
        }).addOnFailureListener(e -> {
            Log.w(TAG, String.format("Error deleting photograph with id %s: ", photograph.getId().toString()), e);
            onFailureListener.onFailure(e);
        });
    }

    /**
     * Gets the name of the collection.
     *
     * @param collection The collection.
     * @return The name of the collection.
     */
    protected String getCollectionName(Collection collection) {
        switch (collection) {
            case BOOKS:
                return "books";
            case USERS:
                return "users";
            case REQUESTS:
                return "requests";
            case PHOTOGRAPHS:
                return "photographs";
            default:
                throw new IllegalArgumentException("Unrecognized collection.");
        }
    }

    private void storeEntity(Collection collection, FirestoreIndexable entity, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        db.collection(getCollectionName(collection))
            .document(entity.getId().toString())
            .set(entity.toFirestoreDocument())
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, String.format("Stored %s with id %s to collection %s.", entity.getClass().getName().toLowerCase(), entity.getId(), collection));
                onSuccessListener.onSuccess(aVoid);
            })
            .addOnFailureListener(e -> {
                Log.w(TAG, String.format("Error storing %s with id %s to collection %s: ", entity.getClass().getName().toLowerCase(), entity.getId(), collection), e);
                onFailureListener.onFailure(e);
            });
    }

    private <T> void retrieveEntity(Collection collection, String id, FirestoreDeserializer<T> deserializer, OnSuccessListener<T> onSuccessListener, OnFailureListener onFailureListener) {
        db.collection(getCollectionName(collection))
            .document(id)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Log.d(TAG, String.format("Retrieved entity with id %s from collection %s.", id, collection));
                    onSuccessListener.onSuccess(deserializer.deserialize(id, documentSnapshot.getData()));
                } else {
                    Log.d(TAG, String.format("No entity with id %s exists in collection %s.", id, collection));
                    onSuccessListener.onSuccess(null);
                }
            })
            .addOnFailureListener(e -> {
                Log.d(TAG, String.format("Error retrieving entity with id %s from collection %s: ", id, collection), e);
                onFailureListener.onFailure(e);
            });
    }

    private <T> void retrieveEntities(Collection collection, FirestoreDeserializer<T> deserializer, OnSuccessListener<List<T>> onSuccessListener, OnFailureListener onFailureListener) {
        db.collection(getCollectionName(collection))
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<T> entities = new ArrayList<>();
                for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                    entities.add(deserializer.deserialize(queryDocumentSnapshot.getId(), queryDocumentSnapshot.getData()));
                }
                Log.d(TAG, String.format("Retrieved entities from collection %s", collection));
                onSuccessListener.onSuccess(entities);
            })
            .addOnFailureListener(e -> {
                Log.d(TAG, String.format("Error retrieving entities from collection %s: ", collection), e);
                onFailureListener.onFailure(e);
            });
    }

    private <T> void retrieveEntitiesMatching(Collection collection, Function<Query, Query> conditions, FirestoreDeserializer<T> deserializer, OnSuccessListener<List<T>> onSuccessListener, OnFailureListener onFailureListener) {
        conditions.apply(db.collection(getCollectionName(collection)))
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<T> entities = new ArrayList<>();
                for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                    entities.add(deserializer.deserialize(queryDocumentSnapshot.getId(), queryDocumentSnapshot.getData()));
                }
                Log.d(TAG, String.format("Retrieved entities from collection %s matching conditions.", collection));
                onSuccessListener.onSuccess(entities);
            })
            .addOnFailureListener(e -> {
                Log.d(TAG, String.format("Error retrieving entities from collection %s matching conditions: ", collection), e);
                onFailureListener.onFailure(e);
            });
    }

    private void deleteEntity(Collection collection, String id, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        db.collection(getCollectionName(collection))
            .document(id)
            .delete()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, String.format("Deleted entity %s from collection %s.", id, collection));
                onSuccessListener.onSuccess(aVoid);
            })
            .addOnFailureListener(e -> {
                Log.d(TAG, String.format("Error deleting entity %s from collection %s: ", id, collection), e);
                onFailureListener.onFailure(e);
            });
    }
}
