package in.mrinmoy.example.authentication.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class PageDetails {
    private Integer currentPage;
    private Long totalItems;
    private Integer totalPages;
    private List<? extends Object> content;
}
