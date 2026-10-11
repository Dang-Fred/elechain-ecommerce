//ReviewResponseDTO.java
package hcmute.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {
    private String tenKH;
    private String avatar;
    private Integer soSao;
    private String binhLuan;
    private LocalDateTime ngayDG;
}