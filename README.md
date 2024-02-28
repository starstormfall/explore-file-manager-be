# File Manager Backend

[[_TOC_]]

This is the file system provider that allows SyncFusion React File Manager component to manage files and folders. Frontend project is [here](https://github.com/starstormfall/explore-syncfusion-fe).

## Development 

The project is developed using `Maven` and `Java`

### Contributing 

To enhance or update the project, follow the development workflow below:
1. Create a Branch
2. Create an Issue
3. Create a Merge Request

### Setup for Localhost
```bash
# Spin up MongoDB & Minio using Docker - required for localhost development
docker compose -f ./localhost/docker-compose.yml up -d
```

## API Endpoints
Access SwaggerUI via `/api-docs`

### For File System Provider

#### `/Upload`
#### `/Download`
#### `/GetImage`
#### `/FileOperations`
| **Action** | **Description**                                                                 |
|------------|---------------------------------------------------------------------------------|
| read       | reads matadata from `mongo` only                                                |
| create     | only creates new folder (different from /Upload), writes to `minio` and `mongo` |
| rename     | updates `minio` and `mongo`                                                     |
| delete     | updates `minio` and `mongo`                                                     |
| details    | reads matadata from `mongo` only                                                |
| search     | reads matadata from `mongo` only                                                |
| copy       | updates `minio` and `mongo`                                                     |
| move       | updates `minio` and `mongo`                                                     |


### For `Mongo` and `Minio` operations
- `api/v1/admin/minio`
- `api/v1/admin/mongo`

## Resources

- [Data Schema](https://dbdiagram.io/d/Products-65de95bc5cd0412774f889fe)
- [Syncfusion Docs - File System Provider](https://ej2.syncfusion.com/react/documentation/file-manager/file-system-provider)
- [Syncfusion Docs - Implement own service provider](https://ej2.syncfusion.com/react/documentation/file-manager/how-to/custom-file-provider)

