package com.example.bookmark.mocks;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;

import com.example.bookmark.models.Book;
import com.example.bookmark.models.Geolocation;
import com.example.bookmark.models.Photograph;
import com.example.bookmark.models.Request;
import com.example.bookmark.models.User;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Mocks users, books, and requests.
 *
 * @author Kyle Hennig.
 */
public class MockModels {
    private static final User mockOwner = new User("john.smith42", "John", "Smith", "jsmith@ualberta.ca", "7801234567");
    private static final User mockRequester = new User("mary.jane9", "Mary", "Jane", "mjane@ualberta.ca", "7809999999");
    private static final Book mockBook1 = new Book(mockOwner, "Code Complete 2", "Steve McConnell", "0-7356-1976-0");
    private static final Book mockBook2 = new Book(mockOwner, "Programming Pearls", "Jon Bentley", "978-0-201-65788-3");
    private static final Book mockBook3 = new Book(mockOwner, "Unedited Title", "Unedited Author", "000000000");
    private static final Book mockBook4 = new Book(mockOwner, "Borrowed Book 1", "John Apple", "000000001");
    private static final Book mockBook5 = new Book(mockOwner, "Accepted Book 1", "John Apple", "000000002");
    private static final Book mockBook6 = new Book(mockOwner, "Borrowed Book 2", "John Apple", "000000003");
    private static final Geolocation mockLocation = new Geolocation(53.5461, -113.4938);
    private static final Request request1 = new Request(mockBook1, mockRequester, mockLocation);
    private static final Request request2 = new Request(mockBook2, mockRequester, mockLocation);
    private static final Request request4 = new Request(mockBook4, mockRequester, mockLocation);
    private static final Request request5 = new Request(mockBook5, mockRequester, mockLocation);
    private static final Request request6 = new Request(mockBook6, mockRequester, mockLocation);
    private static final Photograph mockPhotograph;

    static {
        setupStatuses();
        mockPhotograph = setupPhotograph();
    }

    public static User getMockOwner() {
        return mockOwner;
    }

    public static User getMockRequester() {
        return mockRequester;
    }

    public static Book getMockBook1() {
        return mockBook1;
    }

    public static Book getMockBook2() {
        return mockBook2;
    }

    public static Book getMockBook3() {
        return mockBook3;
    }

    public static Book getMockBook4() {
        return mockBook4;
    }

    public static Book getMockBook5() {
        return mockBook5;
    }

    public static Book getMockBook6() {
        return mockBook6;
    }

    public static Request getMockRequest1() {
        return request1;
    }

    public static Request getMockRequest2() {
        return request2;
    }

    public static Request getMockRequest4() {
        return request4;
    }

    public static Request getMockRequest5() {
        return request5;
    }

    public static Request getMockRequest6() {
        return request6;
    }

    public static Photograph getMockPhotograph() {
        return mockPhotograph;
    }

    private static void setupStatuses() {
        // Sets the book statuses.
        MockModels.getMockBook1().setStatus(Book.Status.REQUESTED);
        MockModels.getMockBook2().setStatus(Book.Status.REQUESTED);
        MockModels.getMockBook4().setStatus(Book.Status.BORROWED);
        MockModels.getMockBook5().setStatus(Book.Status.ACCEPTED);
        MockModels.getMockBook6().setStatus(Book.Status.BORROWED);
        // Sets the request statuses.
        MockModels.getMockRequest4().setStatus(Request.Status.BORROWED);
        MockModels.getMockRequest5().setStatus(Request.Status.ACCEPTED);
        MockModels.getMockRequest6().setStatus(Request.Status.BORROWED);
    }

    private static Photograph setupPhotograph() {
        // Creates a blue square.
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(50, 50, config);
        for (int x = 0; x < bitmap.getWidth(); ++x) {
            for (int y = 0; y < bitmap.getHeight(); ++y) {
                bitmap.setPixel(x, y, Color.BLUE);
            }
        }
        try {
            // Saves the bitmap to a file.
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, bytes);
            File file = File.createTempFile("blue-square", "jpg");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes.toByteArray());
            fos.flush();
            fos.close();
            // Creates the mock photograph.
            Photograph mockPhotograph = new Photograph(Uri.fromFile(file));
            mockBook1.setPhotograph(mockPhotograph);
            mockBook2.setPhotograph(mockPhotograph);
            return mockPhotograph;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
