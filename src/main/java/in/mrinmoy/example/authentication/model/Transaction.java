package in.mrinmoy.example.authentication.model;

import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Document(collection = "Transaction")
public class Transaction {
    @Id
    private String id;
    @NonNull
    private String userId;
    @NonNull
    private String amount;
    @NonNull
    private TransactionType transactionType;
    private CreditType creditType;
    @NonNull
    private String time;
    private String transactionId;
}
