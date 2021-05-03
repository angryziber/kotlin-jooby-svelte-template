import {get, writable} from 'svelte/store'
import router from '@ui/routing/Router'
import {User} from '@ui/api/types'

class Session {
  userStore = writable<User|null>(null)

  get user(): User|null {
    return get(this.userStore)
  }

  set user(user: User|null) {
    this.userStore.set(user)
  }

  set testUser(user: Partial<User>|null) {
    this.user = user as any
  }
}

const session = new Session()

export default session
export const user = session.userStore

export async function finishLogin(user: User, page?: string) {
  session.user = user
  router.navigateTo(page || user.role?.toLowerCase())
}
