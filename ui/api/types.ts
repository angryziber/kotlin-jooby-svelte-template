// auth.AuthRequest
export interface AuthRequest {login: string; password: string;}
// auth.Role
export enum Role {PUBLIC = 'PUBLIC', USER = 'USER', ADMIN = 'ADMIN'}
// auth.User
export interface User {id: string; login: string; role: Role; lang: string; name?: string; email?: string; createdAt: Date|string;}
