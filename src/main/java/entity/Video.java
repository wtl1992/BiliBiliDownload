package entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: wtl
 * @License: (C) Copyright 2020
 * @Contact: 1050100468@qq.com
 * @Date: 2020/8/12 6:56
 * @Version: 1.0
 * @Description:
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Video {
    private String url;
    private String cid;
    private String bVid;
    private String partName;
}
