package com.duan.blogos.service.impl.blogger.profile;

import com.duan.blogos.dao.blogger.BloggerPictureDao;
import com.duan.blogos.entity.blogger.BloggerPicture;
import com.duan.blogos.enums.BloggerPictureCategoryEnum;
import com.duan.blogos.exception.BaseRuntimeException;
import com.duan.blogos.manager.ImageManager;
import com.duan.blogos.result.ResultBean;
import com.duan.blogos.service.blogger.profile.GalleryService;
import com.duan.blogos.util.CollectionUtils;
import com.duan.blogos.util.ImageUtils;
import com.duan.blogos.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created on 2017/12/19.
 *
 * @author DuanJiaNing
 */
@Service
public class GalleryServiceImpl implements GalleryService {

    @Autowired
    private BloggerPictureDao pictureDao;

    @Autowired
    private ImageManager imageManager;

    @Override
    public int insertPicture(int bloggerId, String path, String bewrite, BloggerPictureCategoryEnum category, String title) {
        BloggerPicture picture = new BloggerPicture();
        picture.setBewrite(bewrite);
        picture.setBloggerId(bloggerId);
        picture.setCategory(category.getCode());
        picture.setPath(path);
        picture.setTitle(title);
        int effect = pictureDao.insert(picture);
        if (effect <= 0) return -1;

        return picture.getId();
    }

    @Override
    public int insertPicture(MultipartFile file, int bloggerId, String bewrite, BloggerPictureCategoryEnum category,
                             String title) {

        int cate = category.getCode();
        String path;

        //保存到磁盘
        try {
            path = imageManager.saveImageToDisk(file, bloggerId, cate);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        if (path == null) return -1;

        //数据库新增记录
        String ti = StringUtils.isEmpty(title) ? ImageUtils.getImageName(file.getOriginalFilename()) : title;
        if (BloggerPictureCategoryEnum.isUniqueCategory(cate)) { //唯一类别
            // 如果设备上已经有该唯一图片，将原来的图片移到默认文件夹，同时修改数据库
            removeBloggerUniquePicture(bloggerId, category);
        } else if (category == BloggerPictureCategoryEnum.BLOGGER_AVATAR) {//保证头像唯一
            //腾位置，如果需要的话
            // MAYBUG 当图片一样，同为头像时，旧的头像将移动到默认文件夹，实际上这两张图片有可能是完全一样的
            removeBloggerUniquePicture(bloggerId, BloggerPictureCategoryEnum.BLOGGER_AVATAR);
        }

        //插入新纪录
        return insertPicture(bloggerId, path, bewrite, category, ti);

    }

    /*
     * 腾地方
     * 移到唯一类图片到默认图片文件夹，同时修改数据库记录
     * @param bloggerId 博主id
     */
    private void removeBloggerUniquePicture(int bloggerId, BloggerPictureCategoryEnum uniqueCate) {
        BloggerPicture avatar = pictureDao.getPictureByPropertiesCategory(uniqueCate.getCode());

        if (avatar != null) {
            try {
                //移动原来的头像到默认类别图片所在文件夹
                String newPath = imageManager.moveImage(avatar, bloggerId, BloggerPictureCategoryEnum.DEFAULT);

                //更新数据库记录
                avatar.setCategory(BloggerPictureCategoryEnum.DEFAULT.getCode());
                avatar.setPath(newPath);
                pictureDao.update(avatar);

            } catch (IOException e) {
                e.printStackTrace();
                // 移动文件出错，文件移动情况未知，麻烦大了
                // MAYBUG 回滚数据库操作，但磁盘操作无法预料，也无法处理
                throw new BaseRuntimeException("move image fail when replace avatar");
            }
        }

    }

    @Override
    public boolean deletePicture(int pictureId, boolean deleteOnDisk) {
        BloggerPicture picture = getPicture(pictureId);
        String path = picture.getPath();

        //删除数据库记录
        int effect = pictureDao.delete(pictureId);
        if (effect <= 0) return false;

        if (deleteOnDisk) {
            //删除磁盘文件
            boolean succ = imageManager.deleteImageFromDisk(path);
            // 删除失败时抛出异常，使数据库事务回滚
            if (!succ) throw new BaseRuntimeException("delete image fail");
        }

        return true;
    }

    @Override
    public BloggerPicture getPicture(int pictureId) {
        return pictureDao.getPictureById(pictureId);
    }

    @Override
    public BloggerPicture getPicture(int pictureId, int bloggerId) {
        BloggerPicture picture = pictureDao.getPictureById(pictureId);
        if (picture == null || !picture.getBloggerId().equals(bloggerId)) return null;

        return picture;
    }

    @Override
    public boolean getPictureForCheckExist(int pictureId) {
        return pictureDao.getPictureById(pictureId) != null;
    }

    @Override
    public BloggerPicture getPictureByPropertiesCategory(BloggerPictureCategoryEnum category) {
        return pictureDao.getPictureByPropertiesCategory(category.getCode());
    }

    @Override
    public ResultBean<List<BloggerPicture>> listBloggerPicture(int bloggerId, BloggerPictureCategoryEnum category, int offset, int rows) {

        List<BloggerPicture> result;
        if (category == null) {
            result = pictureDao.listPictureByBloggerId(bloggerId, offset, rows);
        } else {
            result = pictureDao.listPictureByBloggerAndCategory(bloggerId, category.getCode(), offset, rows);
        }

        if (CollectionUtils.isEmpty(result)) return null;

        return new ResultBean<>(result);
    }

    @Override
    public boolean updatePicture(int pictureId, BloggerPictureCategoryEnum category, String bewrite, String title) {

        BloggerPicture oldPicture = pictureDao.getPictureById(pictureId);

        BloggerPicture newPicture = new BloggerPicture();
        newPicture.setBewrite(bewrite);
        newPicture.setBloggerId(oldPicture.getBloggerId());
        newPicture.setCategory(category == null ? oldPicture.getCategory() : category.getCode());
        newPicture.setId(oldPicture.getId());
        newPicture.setTitle(title);

        // 修改设备上图片路径，如果需要的话
        String newPath = null;
        if (category != null && category.getCode() != oldPicture.getCategory()) { // 修改了类别
            int bloggerId = oldPicture.getBloggerId();
            try {

                if (category == BloggerPictureCategoryEnum.BLOGGER_AVATAR) { // 修改为博主头像（唯一）
                    //腾位置，如果需要的话
                    removeBloggerUniquePicture(bloggerId, BloggerPictureCategoryEnum.BLOGGER_AVATAR);

                    //移动到头像目录
                    newPath = imageManager.moveImage(oldPicture, bloggerId, BloggerPictureCategoryEnum.BLOGGER_AVATAR);

                } else if (BloggerPictureCategoryEnum.isUniqueCategory(category.getCode())) {// 唯一类别
                    //腾位置，如果需要的话
                    removeBloggerUniquePicture(bloggerId, category);

                    //移动到目标目录
                    newPath = imageManager.moveImage(oldPicture, bloggerId, category);
                } else { // 普通目录
                    newPath = imageManager.moveImage(oldPicture, bloggerId, category);
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new BaseRuntimeException("move image fail when change image category");
            }
        }

        newPicture.setPath(newPath == null ? oldPicture.getPath() : newPath);
        int effect = pictureDao.update(newPicture);
        if (effect <= 0) return false;

        return true;
    }

}
