package folletto.toyproject.domain.group.entity;

import folletto.toyproject.domain.group.dto.GroupRequest;
import lombok.*;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Table(name = "groups")
public class GroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupId;

    private String groupName;
    private String description;
    private boolean isMasterGroup;

    @Builder
    public GroupEntity(String groupName, String description, boolean isMasterGroup) {
        this.groupName = groupName;
        this.description = description;
        this.isMasterGroup = isMasterGroup;
    }

    public static GroupEntity from(GroupRequest groupRequest) {
        return GroupEntity.builder()
                .groupName(groupRequest.groupName())
                .description(groupRequest.description())
                .isMasterGroup(false)
                .build();
    }

    public void update(GroupRequest groupRequest) {
        this.groupName = groupRequest.groupName();
        this.description = groupRequest.description();
    }
}
