import {get, writable} from 'svelte/store'
import router from '../routing/Router'

export class User {
  id!: string
  login!: string
  role!: string
  name?: string
}

class Session {
  userStore = writable<User|null>(null)

  get user(): User|null {
    return get(this.userStore)
  }

  set user(user: User|null) {
    if (user) Object.setPrototypeOf(user, new User())
    this.userStore.set(user)
  }

  set testUser(user: Partial<User>|null) {
    this.user = user as any
  }
}

const session = new Session()

export default session
export const user = session.userStore

export async function finishLogin(user, page?) {
  session.user = user
  router.navigateTo(page || user.role)
}
