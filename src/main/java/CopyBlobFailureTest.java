import java.util.Locale;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.common.StorageSharedKeyCredential;

public class CopyBlobFailureTest {


    public static final boolean WITH_WORKAROUND = false;

    public static void main(String[] args) {
        String accountName = System.getenv("TEST_ACCOUNT_NAME");
        String accountKey = System.getenv("TEST_ACCOUNT_KEY");

        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);
        String endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);

        BlobServiceClient storageClient = new BlobServiceClientBuilder().endpoint(endpoint).credential(credential).buildClient();

        BlobContainerClient sourceContainer = storageClient.getBlobContainerClient("public");
        BlobContainerClient targetContainer = storageClient.getBlobContainerClient("public-target");

        final ListBlobsOptions options = new ListBlobsOptions()
                .setMaxResultsPerPage(1000)
                .setDetails(new BlobListDetails().setRetrieveMetadata(true));

        final PagedIterable<BlobItem> blobItems = sourceContainer.listBlobs(options, null);

        for (BlobItem sourceBlobItem : blobItems) {
            final BlobClient sourceBlobClient;
            final BlobClient targetBlobClient;
            if (WITH_WORKAROUND) { // set to true and copy process is successful
                sourceBlobClient = sourceContainer.getBlobClient(fixNameEncoding(sourceBlobItem.getName()));

                // If you do not fix the name for the target too, the copy process is successful, but target-file name is 'test file.txt' instead of 'test%20file.txt'
                targetBlobClient = targetContainer.getBlobClient(fixNameEncoding(sourceBlobItem.getName()));
            } else {
                sourceBlobClient = sourceContainer.getBlobClient(sourceBlobItem.getName());
                targetBlobClient = targetContainer.getBlobClient(sourceBlobItem.getName());
            }

            try {
                System.out.println(String.format("copyFromUrl: SourceName [%s], SourceURL: [%s]", sourceBlobItem.getName(), sourceBlobClient.getBlobUrl()));
                targetBlobClient.copyFromUrl(sourceBlobClient.getBlobUrl());
                System.out.println(String.format("copyFromUrl: SourceName [%s] - success", sourceBlobItem.getName()));
            } catch (BlobStorageException e) {
                System.err.println("Failed: " + e.getMessage());
            }
        }
    }

    public static String fixNameEncoding(final String name) {
        return name.replace("%", "%25");
    }
}

