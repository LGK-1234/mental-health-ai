package org.example.aispingboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.aispingboot.common.Result;
import org.example.aispingboot.common.ResultCode;
import org.example.aispingboot.entity.KnowledgeArticle;
import org.example.aispingboot.entity.KnowledgeCategory;
import org.example.aispingboot.mapper.KnowledgeArticleMapper;
import org.example.aispingboot.mapper.KnowledgeCategoryMapper;
import org.example.aispingboot.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 心理健康知识库：文章分页/详情/增删改、分类树。
 */
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    @Autowired
    private KnowledgeArticleMapper articleMapper;

    @Autowired
    private KnowledgeCategoryMapper categoryMapper;

    @GetMapping("/article/page")
    public Result<Map<String, Object>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize,
                                            @RequestParam(required = false) String title,
                                            @RequestParam(required = false) Long categoryId,
                                            @RequestParam(required = false) Integer status) {
        Page<KnowledgeArticle> page = new Page<>(pageNum, pageSize);
        articleMapper.selectPage(page, new LambdaQueryWrapper<KnowledgeArticle>()
                .like(StringUtils.hasText(title), KnowledgeArticle::getTitle, title)
                .eq(categoryId != null, KnowledgeArticle::getCategoryId, categoryId)
                .eq(status != null, KnowledgeArticle::getStatus, status)
                .orderByDesc(KnowledgeArticle::getCreatedAt));

        Map<String, Object> data = new HashMap<>();
        data.put("records", page.getRecords());
        data.put("total", page.getTotal());
        data.put("current", page.getCurrent());
        data.put("size", page.getSize());
        return Result.ok(data);
    }

    @GetMapping("/article/{id}")
    public Result<KnowledgeArticle> detail(@PathVariable String id) {
        KnowledgeArticle article = articleMapper.selectById(id);
        if (article == null) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "文章不存在", null);
        }
        // 阅读量 +1
        KnowledgeArticle update = new KnowledgeArticle();
        update.setId(id);
        update.setReadCount((article.getReadCount() == null ? 0 : article.getReadCount()) + 1);
        articleMapper.updateById(update);
        article.setReadCount(update.getReadCount());
        return Result.ok(article);
    }

    /** 新增文章 */
    @PostMapping("/article")
    public Result<KnowledgeArticle> create(@RequestBody KnowledgeArticle article) {
        Long userId = JwtTokenUtil.getCurrentUserIdOrNull();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMsg(), null);
        }
        if (!StringUtils.hasText(article.getTitle()) || !StringUtils.hasText(article.getContent())) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "标题和正文不能为空", null);
        }
        if (!StringUtils.hasText(article.getId())) {
            article.setId(UUID.randomUUID().toString());
        }
        article.setAuthorId(userId);
        article.setReadCount(0);
        if (article.getStatus() == null) {
            article.setStatus(0);
        }
        if (Integer.valueOf(1).equals(article.getStatus())) {
            article.setPublishedAt(LocalDateTime.now());
        }
        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());
        articleMapper.insert(article);
        return Result.ok(article);
    }

    /** 更新文章 */
    @PutMapping("/article/{id}")
    public Result<KnowledgeArticle> update(@PathVariable String id, @RequestBody KnowledgeArticle article) {
        KnowledgeArticle exist = articleMapper.selectById(id);
        if (exist == null) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "文章不存在", null);
        }
        article.setId(id);
        article.setUpdatedAt(LocalDateTime.now());
        if (Integer.valueOf(1).equals(article.getStatus()) && exist.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }
        articleMapper.updateById(article);
        return Result.ok(articleMapper.selectById(id));
    }

    /** 修改文章状态：0 草稿 / 1 发布 / 2 下线 */
    @PutMapping("/article/{id}/status")
    public Result<String> changeStatus(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Object statusObj = body.get("status");
        if (statusObj == null) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "status 不能为空", null);
        }
        KnowledgeArticle update = new KnowledgeArticle();
        update.setId(id);
        update.setStatus(Integer.valueOf(statusObj.toString()));
        update.setUpdatedAt(LocalDateTime.now());
        if (Integer.valueOf(1).equals(update.getStatus())) {
            update.setPublishedAt(LocalDateTime.now());
        }
        articleMapper.updateById(update);
        return Result.ok("状态已更新");
    }

    @DeleteMapping("/article/{id}")
    public Result<String> delete(@PathVariable String id) {
        articleMapper.deleteById(id);
        return Result.ok("已删除");
    }

    /** 分类树：parent_id = 0 为根节点 */
    @GetMapping("/category/tree")
    public Result<List<KnowledgeCategory>> tree() {
        List<KnowledgeCategory> all = categoryMapper.selectList(new LambdaQueryWrapper<KnowledgeCategory>()
                .eq(KnowledgeCategory::getStatus, 1)
                .orderByAsc(KnowledgeCategory::getSortOrder));

        Map<Long, KnowledgeCategory> index = new HashMap<>();
        all.forEach(c -> index.put(c.getId(), c));

        List<KnowledgeCategory> roots = new ArrayList<>();
        for (KnowledgeCategory c : all) {
            Long parentId = c.getParentId();
            if (parentId == null || parentId == 0L || !index.containsKey(parentId)) {
                roots.add(c);
            } else {
                index.get(parentId).getChildren().add(c);
            }
        }
        return Result.ok(roots);
    }
}
